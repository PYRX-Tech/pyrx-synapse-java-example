package tech.pyrx.synapse;

import tech.pyrx.synapse.errors.ErrorMapper;
import tech.pyrx.synapse.errors.SynapseError;
import tech.pyrx.synapse.errors.SynapseRateLimitError;
import tech.pyrx.synapse.internal.JsonUtil;
import tech.pyrx.synapse.model.*;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.LongConsumer;

/**
 * Main Synapse API client.
 */
public class SynapseClient implements AutoCloseable {

    public static final String VERSION = "0.1.0";
    public static final String DEFAULT_BASE_URL = "https://synapse-api.pyrx.tech";

    private static final double MAX_BACKOFF_SEC = 30.0;
    private static final double JITTER_MAX_SEC = 0.5;
    private static final Set<Integer> RETRYABLE_STATUSES = Set.of(429, 500, 502, 503, 504);

    private final String apiKey;
    private final String workspaceId;
    private final String baseUrl;
    private final int timeoutSeconds;
    private final int maxRetries;
    private final String environment;
    private final HttpClient httpClient;

    public final ContactsClient contacts;
    public final TemplatesClient templates;

    // Visible for testing: override to avoid real sleeps.
    LongConsumer sleepFn = millis -> {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    };

    public SynapseClient(SynapseConfig config) {
        if (config.getApiKey() == null || config.getApiKey().trim().isEmpty()) {
            throw new IllegalArgumentException("Synapse: api_key is required");
        }
        if (config.getWorkspaceId() == null || config.getWorkspaceId().trim().isEmpty()) {
            throw new IllegalArgumentException("Synapse: workspace_id is required");
        }

        this.apiKey = config.getApiKey();
        this.workspaceId = config.getWorkspaceId();

        String base = config.getBaseUrl();
        if (base == null || base.isEmpty()) {
            base = DEFAULT_BASE_URL;
        }
        // Strip trailing slash
        while (base.endsWith("/")) {
            base = base.substring(0, base.length() - 1);
        }
        this.baseUrl = base;

        this.timeoutSeconds = config.getTimeoutSeconds() > 0 ? config.getTimeoutSeconds() : 30;
        this.maxRetries = config.getMaxRetries();
        this.environment = detectEnvironment(apiKey);

        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(this.timeoutSeconds))
            .build();

        this.contacts = new ContactsClient(this);
        this.templates = new TemplatesClient(this);
    }

    public String getBaseUrl() { return baseUrl; }
    public String getEnvironment() { return environment; }

    // -----------------------------------------------------------------------
    // Public API methods
    // -----------------------------------------------------------------------

    public TrackResponse track(TrackParams params) {
        Map<String, Object> resp = request("POST", "/v1/events", params.toMap());
        return TrackResponse.fromMap(resp);
    }

    public BatchTrackResponse trackBatch(TrackBatchParams params) {
        Map<String, Object> resp = request("POST", "/v1/events/batch", params.toMap());
        return BatchTrackResponse.fromMap(resp);
    }

    public ContactResponse identify(IdentifyParams params) {
        Map<String, Object> resp = request("POST", "/v1/contacts", params.toMap());
        return ContactResponse.fromMap(resp);
    }

    public BulkContactResponse identifyBatch(IdentifyBatchParams params) {
        Map<String, Object> resp = request("POST", "/v1/contacts/bulk", params.toMap());
        return BulkContactResponse.fromMap(resp);
    }

    public SendResponse send(SendParams params) {
        Map<String, Object> resp = request("POST", "/v1/send", params.toMap());
        return SendResponse.fromMap(resp);
    }

    // -----------------------------------------------------------------------
    // Internal HTTP methods
    // -----------------------------------------------------------------------

    Map<String, Object> request(String method, String path, Map<String, Object> body) {
        return requestWithParams(method, path, body, null);
    }

    /**
     * Performs an HTTP request with optional query parameters and retry logic.
     * Returns null for 204 responses.
     */
    Map<String, Object> requestWithParams(String method, String path, Map<String, Object> body,
                                           Map<String, String> queryParams) {
        String fullUrl = baseUrl + path;
        if (queryParams != null && !queryParams.isEmpty()) {
            StringBuilder qs = new StringBuilder("?");
            boolean first = true;
            for (Map.Entry<String, String> entry : queryParams.entrySet()) {
                if (!first) qs.append("&");
                first = false;
                qs.append(urlEncode(entry.getKey())).append("=").append(urlEncode(entry.getValue()));
            }
            fullUrl += qs.toString();
        }

        RuntimeException lastErr = null;

        for (int attempt = 0; attempt <= maxRetries; attempt++) {
            try {
                HttpRequest.Builder reqBuilder = HttpRequest.newBuilder()
                    .uri(URI.create(fullUrl))
                    .timeout(Duration.ofSeconds(timeoutSeconds))
                    .header("X-Workspace-Id", workspaceId)
                    .header("X-Api-Key", apiKey)
                    .header("Content-Type", "application/json")
                    .header("User-Agent", "pyrx-synapse-java/" + VERSION);

                if (body != null) {
                    String jsonBody = JsonUtil.serialize(body);
                    reqBuilder.method(method, HttpRequest.BodyPublishers.ofString(jsonBody));
                } else {
                    reqBuilder.method(method, HttpRequest.BodyPublishers.noBody());
                }

                HttpResponse<String> response = httpClient.send(reqBuilder.build(),
                    HttpResponse.BodyHandlers.ofString());

                int statusCode = response.statusCode();

                // 204 No Content
                if (statusCode == 204) {
                    return null;
                }

                String respBody = response.body();

                // Try to parse as object first, then as array
                Map<String, Object> parsed = null;
                if (respBody != null && !respBody.trim().isEmpty()) {
                    String trimmed = respBody.trim();
                    if (trimmed.startsWith("[")) {
                        // Array response (e.g., template list)
                        // We wrap it in a synthetic object so callers can handle it
                        List<Object> arr = JsonUtil.deserializeArray(trimmed);
                        if (statusCode >= 400) {
                            parsed = Map.of();
                        } else {
                            // Return a special wrapper map with "_array" key
                            parsed = Map.of("_array", arr);
                            if (statusCode < 400) {
                                return parsed;
                            }
                        }
                    } else if (trimmed.startsWith("{")) {
                        try {
                            parsed = JsonUtil.deserializeObject(trimmed);
                        } catch (Exception e) {
                            parsed = Map.of();
                        }
                    } else {
                        parsed = Map.of();
                    }
                } else {
                    parsed = Map.of();
                }

                if (statusCode >= 400) {
                    double retryAfter = 0;
                    String raHeader = response.headers().firstValue("Retry-After").orElse(null);
                    if (raHeader != null) {
                        try {
                            retryAfter = Double.parseDouble(raHeader);
                        } catch (NumberFormatException ignored) {}
                    }

                    SynapseError apiErr = ErrorMapper.mapError(statusCode, parsed, retryAfter);
                    lastErr = apiErr;

                    if (!RETRYABLE_STATUSES.contains(statusCode) || attempt >= maxRetries) {
                        throw apiErr;
                    }

                    sleepFn.accept(backoffDelayMs(attempt, apiErr));
                    continue;
                }

                // Success
                return parsed;

            } catch (SynapseError e) {
                throw e;
            } catch (ConnectException | HttpTimeoutException e) {
                lastErr = new RuntimeException(e);
                if (attempt >= maxRetries) {
                    break;
                }
                sleepFn.accept(backoffDelayMs(attempt, null));
            } catch (IOException | InterruptedException e) {
                lastErr = new RuntimeException(e);
                if (e instanceof InterruptedException) {
                    Thread.currentThread().interrupt();
                }
                if (attempt >= maxRetries) {
                    break;
                }
                sleepFn.accept(backoffDelayMs(attempt, null));
            }
        }

        if (lastErr instanceof SynapseError) {
            throw (SynapseError) lastErr;
        }
        throw lastErr != null ? lastErr : new RuntimeException("Request failed");
    }

    @Override
    public void close() {
        // HttpClient does not require explicit close in Java 11
    }

    // -----------------------------------------------------------------------
    // Helpers
    // -----------------------------------------------------------------------

    private static String detectEnvironment(String apiKey) {
        if (apiKey.startsWith("psk_test_")) return "test";
        if (apiKey.startsWith("psk_live_")) return "live";
        return "unknown";
    }

    private static long backoffDelayMs(int attempt, RuntimeException err) {
        if (err instanceof SynapseRateLimitError) {
            double retryAfter = ((SynapseRateLimitError) err).getRetryAfter();
            if (retryAfter > 0) {
                double secs = retryAfter + ThreadLocalRandom.current().nextDouble() * JITTER_MAX_SEC;
                return (long) (secs * 1000);
            }
        }
        double exponential = 1.0 * Math.pow(2, attempt);
        double capped = Math.min(exponential, MAX_BACKOFF_SEC);
        double secs = capped + ThreadLocalRandom.current().nextDouble() * JITTER_MAX_SEC;
        return (long) (secs * 1000);
    }

    private static String urlEncode(String value) {
        // Simple URL encoding for query parameters
        StringBuilder sb = new StringBuilder();
        for (char c : value.toCharArray()) {
            if ((c >= 'a' && c <= 'z') || (c >= 'A' && c <= 'Z') || (c >= '0' && c <= '9')
                || c == '-' || c == '_' || c == '.' || c == '~') {
                sb.append(c);
            } else {
                byte[] bytes = String.valueOf(c).getBytes(java.nio.charset.StandardCharsets.UTF_8);
                for (byte b : bytes) {
                    sb.append('%');
                    sb.append(String.format("%02X", b & 0xFF));
                }
            }
        }
        return sb.toString();
    }
}
