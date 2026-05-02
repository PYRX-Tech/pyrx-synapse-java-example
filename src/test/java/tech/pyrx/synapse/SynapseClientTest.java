package tech.pyrx.synapse;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import org.junit.jupiter.api.Test;
import tech.pyrx.synapse.errors.*;
import tech.pyrx.synapse.model.*;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.*;

class SynapseClientTest {

    private static final String TEST_API_KEY = "psk_test_abc123";
    private static final String TEST_WORKSPACE_ID = "ws_test_1";

    // -----------------------------------------------------------------------
    // Test helper: creates a client pointed at a local HTTP server
    // -----------------------------------------------------------------------

    interface RequestHandler {
        void handle(HttpExchange exchange) throws IOException;
    }

    private SynapseClient newTestClient(RequestHandler handler) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(0), 0);
        int port = server.getAddress().getPort();
        server.createContext("/", exchange -> {
            try {
                handler.handle(exchange);
            } catch (Exception e) {
                exchange.sendResponseHeaders(500, 0);
                exchange.getResponseBody().close();
            }
        });
        server.start();

        SynapseConfig config = new SynapseConfig()
            .apiKey(TEST_API_KEY)
            .workspaceId(TEST_WORKSPACE_ID)
            .baseUrl("http://localhost:" + port)
            .timeoutSeconds(5)
            .maxRetries(1);

        SynapseClient client = new SynapseClient(config);
        // Disable real sleeps in tests
        client.sleepFn = millis -> {};

        // Register shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> server.stop(0)));

        return client;
    }

    private static void respond(HttpExchange exchange, int status, String body) throws IOException {
        byte[] bytes = body.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(status, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static void respondNoBody(HttpExchange exchange, int status) throws IOException {
        exchange.sendResponseHeaders(status, -1);
        exchange.getResponseBody().close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        return new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
    }

    // -----------------------------------------------------------------------
    // Constructor validation
    // -----------------------------------------------------------------------

    @Test
    void constructorRejectsEmptyApiKey() {
        SynapseConfig config = new SynapseConfig().apiKey("").workspaceId("ws_1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new SynapseClient(config));
        assertTrue(ex.getMessage().contains("api_key"));
    }

    @Test
    void constructorRejectsNullApiKey() {
        SynapseConfig config = new SynapseConfig().workspaceId("ws_1");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new SynapseClient(config));
        assertTrue(ex.getMessage().contains("api_key"));
    }

    @Test
    void constructorRejectsEmptyWorkspaceId() {
        SynapseConfig config = new SynapseConfig().apiKey("key").workspaceId("");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () -> new SynapseClient(config));
        assertTrue(ex.getMessage().contains("workspace_id"));
    }

    @Test
    void detectsTestEnvironment() {
        SynapseConfig config = new SynapseConfig().apiKey("psk_test_abc").workspaceId("ws_1");
        SynapseClient client = new SynapseClient(config);
        assertEquals("test", client.getEnvironment());
    }

    @Test
    void detectsLiveEnvironment() {
        SynapseConfig config = new SynapseConfig().apiKey("psk_live_abc").workspaceId("ws_1");
        SynapseClient client = new SynapseClient(config);
        assertEquals("live", client.getEnvironment());
    }

    @Test
    void detectsUnknownEnvironment() {
        SynapseConfig config = new SynapseConfig().apiKey("custom_key").workspaceId("ws_1");
        SynapseClient client = new SynapseClient(config);
        assertEquals("unknown", client.getEnvironment());
    }

    @Test
    void stripsTrailingSlash() {
        SynapseConfig config = new SynapseConfig().apiKey("key").workspaceId("ws_1").baseUrl("https://api.test.com/");
        SynapseClient client = new SynapseClient(config);
        assertEquals("https://api.test.com", client.getBaseUrl());
    }

    @Test
    void exposesSubClients() {
        SynapseConfig config = new SynapseConfig().apiKey("key").workspaceId("ws_1");
        SynapseClient client = new SynapseClient(config);
        assertNotNull(client.contacts);
        assertNotNull(client.templates);
    }

    // -----------------------------------------------------------------------
    // Headers
    // -----------------------------------------------------------------------

    @Test
    void sendsCorrectHeaders() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals(TEST_WORKSPACE_ID, exchange.getRequestHeaders().getFirst("X-Workspace-Id"));
            assertEquals(TEST_API_KEY, exchange.getRequestHeaders().getFirst("X-Api-Key"));
            assertEquals("application/json", exchange.getRequestHeaders().getFirst("Content-Type"));
            assertEquals("pyrx-synapse-java/" + SynapseClient.VERSION,
                exchange.getRequestHeaders().getFirst("User-Agent"));
            respond(exchange, 200, "{\"event_id\":\"e_1\",\"status\":\"ok\"}");
        });

        client.track(TrackParams.builder().externalId("u1").eventName("test").build());
    }

    // -----------------------------------------------------------------------
    // Track
    // -----------------------------------------------------------------------

    @Test
    void track() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/events", exchange.getRequestURI().getPath());
            respond(exchange, 200, "{\"event_id\":\"evt_1\",\"status\":\"accepted\"}");
        });

        TrackResponse result = client.track(
            TrackParams.builder().externalId("user_1").eventName("purchase").build());
        assertEquals("evt_1", result.getEventId());
        assertEquals("accepted", result.getStatus());
    }

    @Test
    void trackOnlySendsNonEmptyFields() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            String body = readRequestBody(exchange);
            assertTrue(body.contains("\"external_id\""));
            assertTrue(body.contains("\"event_name\""));
            assertFalse(body.contains("\"attributes\""));
            assertFalse(body.contains("\"contact\""));
            respond(exchange, 200, "{\"event_id\":\"e1\",\"status\":\"ok\"}");
        });

        client.track(TrackParams.builder().externalId("u1").eventName("click").build());
    }

    @Test
    void trackIncludesOptionalFields() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            String body = readRequestBody(exchange);
            assertTrue(body.contains("\"attributes\""));
            assertTrue(body.contains("\"idempotency_key\":\"idk_1\""));
            assertTrue(body.contains("\"occurred_at\":\"2026-01-01T00:00:00Z\""));
            respond(exchange, 200, "{\"event_id\":\"e1\",\"status\":\"ok\"}");
        });

        client.track(TrackParams.builder()
            .externalId("u1")
            .eventName("purchase")
            .attributes(Map.of("amount", 99))
            .idempotencyKey("idk_1")
            .occurredAt("2026-01-01T00:00:00Z")
            .build());
    }

    // -----------------------------------------------------------------------
    // Track Batch
    // -----------------------------------------------------------------------

    @Test
    void trackBatch() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/events/batch", exchange.getRequestURI().getPath());
            respond(exchange, 200, "{\"accepted\":2,\"rejected\":0}");
        });

        BatchTrackResponse result = client.trackBatch(new TrackBatchParams(List.of(
            TrackParams.builder().externalId("u1").eventName("click").build(),
            TrackParams.builder().externalId("u2").eventName("view").build()
        )));
        assertEquals(2, result.getAccepted());
        assertEquals(0, result.getRejected());
    }

    // -----------------------------------------------------------------------
    // Identify
    // -----------------------------------------------------------------------

    @Test
    void identify() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/contacts", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"id\":\"c_1\",\"external_id\":\"u_1\",\"email\":\"a@b.com\",\"created_at\":\"2026-01-01\",\"updated_at\":\"2026-01-01\"}");
        });

        ContactResponse result = client.identify(
            IdentifyParams.builder().externalId("u_1").email("a@b.com").build());
        assertEquals("c_1", result.getId());
        assertEquals("u_1", result.getExternalId());
        assertEquals("a@b.com", result.getEmail());
    }

    // -----------------------------------------------------------------------
    // Identify Batch
    // -----------------------------------------------------------------------

    @Test
    void identifyBatch() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/contacts/bulk", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"total\":2,\"created\":1,\"updated\":1,\"skipped\":0,\"errors\":[]}");
        });

        BulkContactResponse result = client.identifyBatch(new IdentifyBatchParams(
            List.of(IdentifyParams.builder().externalId("u1").build())
        ));
        assertEquals(2, result.getTotal());
        assertEquals(1, result.getCreated());
    }

    // -----------------------------------------------------------------------
    // Send
    // -----------------------------------------------------------------------

    @Test
    void send() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/send", exchange.getRequestURI().getPath());
            respond(exchange, 200, "{\"email_log_id\":\"el_1\",\"status\":\"queued\"}");
        });

        SendResponse result = client.send(SendParams.builder()
            .templateSlug("welcome")
            .to(Map.of("email", "a@b.com"))
            .build());
        assertEquals("el_1", result.getEmailLogId());
        assertEquals("queued", result.getStatus());
    }

    // -----------------------------------------------------------------------
    // Contacts sub-client
    // -----------------------------------------------------------------------

    @Test
    void contactsList() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            assertEquals("/v1/contacts", exchange.getRequestURI().getPath());
            String query = exchange.getRequestURI().getQuery();
            assertTrue(query.contains("page=2"));
            assertTrue(query.contains("per_page=10"));
            respond(exchange, 200,
                "{\"data\":[{\"id\":\"c_1\",\"external_id\":\"u_1\"}],\"meta\":{\"total\":1,\"page\":2,\"per_page\":10,\"total_pages\":1}}");
        });

        ContactListResponse result = client.contacts.list(
            ContactListParams.builder().page(2).perPage(10).build());
        assertEquals(1, result.getData().size());
        assertEquals(2, result.getMeta().getPage());
    }

    @Test
    void contactsGet() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("/v1/contacts/c_1", exchange.getRequestURI().getPath());
            respond(exchange, 200, "{\"id\":\"c_1\",\"external_id\":\"u_1\"}");
        });

        ContactResponse result = client.contacts.get("c_1");
        assertEquals("c_1", result.getId());
    }

    @Test
    void contactsUpdate() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("PATCH", exchange.getRequestMethod());
            assertEquals("/v1/contacts/u_1", exchange.getRequestURI().getPath());
            respond(exchange, 200, "{\"id\":\"c_1\",\"external_id\":\"u_1\",\"email\":\"new@b.com\"}");
        });

        ContactResponse result = client.contacts.update("u_1",
            ContactUpdateParams.builder().email("new@b.com").build());
        assertEquals("new@b.com", result.getEmail());
    }

    @Test
    void contactsDelete() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("DELETE", exchange.getRequestMethod());
            assertEquals("/v1/contacts/u_1", exchange.getRequestURI().getPath());
            respondNoBody(exchange, 204);
        });

        // Should not throw
        client.contacts.delete("u_1");
    }

    // -----------------------------------------------------------------------
    // Templates sub-client
    // -----------------------------------------------------------------------

    @Test
    void templatesList() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("GET", exchange.getRequestMethod());
            assertEquals("/v1/templates", exchange.getRequestURI().getPath());
            respond(exchange, 200, "[{\"id\":\"t_1\",\"name\":\"Welcome\",\"slug\":\"welcome\"}]");
        });

        List<TemplateResponse> result = client.templates.list();
        assertEquals(1, result.size());
        assertEquals("welcome", result.get(0).getSlug());
    }

    @Test
    void templatesGet() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("/v1/templates/welcome", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"id\":\"t_1\",\"name\":\"Welcome\",\"slug\":\"welcome\",\"subject\":\"Hello\"}");
        });

        TemplateResponse result = client.templates.get("welcome");
        assertEquals("Hello", result.getSubject());
    }

    @Test
    void templatesCreate() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/templates", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"id\":\"t_2\",\"name\":\"New\",\"slug\":\"new-tmpl\",\"subject\":\"Hi\"}");
        });

        TemplateResponse result = client.templates.create(TemplateCreateParams.builder()
            .name("New").slug("new-tmpl").subject("Hi").build());
        assertEquals("new-tmpl", result.getSlug());
    }

    @Test
    void templatesUpdate() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("PUT", exchange.getRequestMethod());
            assertEquals("/v1/templates/welcome", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"id\":\"t_1\",\"name\":\"Welcome v2\",\"slug\":\"welcome\",\"subject\":\"Updated\"}");
        });

        TemplateResponse result = client.templates.update("welcome",
            TemplateUpdateParams.builder().subject("Updated").build());
        assertEquals("Updated", result.getSubject());
    }

    @Test
    void templatesDelete() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("DELETE", exchange.getRequestMethod());
            assertEquals("/v1/templates/welcome", exchange.getRequestURI().getPath());
            respondNoBody(exchange, 204);
        });

        client.templates.delete("welcome");
    }

    @Test
    void templatesPreview() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            assertEquals("POST", exchange.getRequestMethod());
            assertEquals("/v1/templates/welcome/preview", exchange.getRequestURI().getPath());
            respond(exchange, 200,
                "{\"subject\":\"Hello Jane\",\"html\":\"<p>Hi</p>\",\"suppressed\":false,\"suppressed_reason\":null}");
        });

        TemplatePreviewResponse result = client.templates.preview("welcome",
            TemplatePreviewParams.builder().build());
        assertEquals("Hello Jane", result.getSubject());
        assertFalse(result.isSuppressed());
    }

    // -----------------------------------------------------------------------
    // Error mapping via client
    // -----------------------------------------------------------------------

    @Test
    void authError401() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            respond(exchange, 401, "{\"message\":\"unauthorized\"}");
        });

        SynapseAuthError err = assertThrows(SynapseAuthError.class, () ->
            client.track(TrackParams.builder().externalId("u1").eventName("test").build()));
        assertEquals(401, err.getStatus());
        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void rateLimitError429() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            exchange.getResponseHeaders().set("Retry-After", "30");
            respond(exchange, 429, "{\"message\":\"rate limited\"}");
        });

        assertThrows(SynapseRateLimitError.class, () ->
            client.track(TrackParams.builder().externalId("u1").eventName("test").build()));
    }

    @Test
    void validationError422() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            respond(exchange, 422,
                "{\"message\":\"validation failed\",\"errors\":[{\"field\":\"email\",\"message\":\"invalid\"}]}");
        });

        SynapseValidationError err = assertThrows(SynapseValidationError.class, () ->
            client.track(TrackParams.builder().externalId("u1").eventName("test").build()));
        assertEquals(1, err.getErrors().size());
    }

    // -----------------------------------------------------------------------
    // Retry logic
    // -----------------------------------------------------------------------

    @Test
    void retries500ThenSucceeds() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        SynapseClient client = newTestClient(exchange -> {
            int count = callCount.incrementAndGet();
            if (count == 1) {
                respond(exchange, 500, "{\"message\":\"internal error\"}");
            } else {
                respond(exchange, 200, "{\"event_id\":\"e1\",\"status\":\"ok\"}");
            }
        });

        TrackResponse result = client.track(
            TrackParams.builder().externalId("u1").eventName("test").build());
        assertEquals(2, callCount.get());
        assertEquals("e1", result.getEventId());
    }

    @Test
    void noRetryOn401() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        SynapseClient client = newTestClient(exchange -> {
            callCount.incrementAndGet();
            respond(exchange, 401, "{\"message\":\"unauthorized\"}");
        });

        assertThrows(SynapseAuthError.class, () ->
            client.track(TrackParams.builder().externalId("u1").eventName("test").build()));
        assertEquals(1, callCount.get());
    }

    @Test
    void retries502GivesUpAfterMaxRetries() throws Exception {
        AtomicInteger callCount = new AtomicInteger(0);
        SynapseClient client = newTestClient(exchange -> {
            callCount.incrementAndGet();
            respond(exchange, 502, "{\"message\":\"bad gateway\"}");
        });

        assertThrows(SynapseError.class, () ->
            client.track(TrackParams.builder().externalId("u1").eventName("test").build()));
        // maxRetries=1 means 2 total attempts
        assertEquals(2, callCount.get());
    }

    // -----------------------------------------------------------------------
    // 204 handling
    // -----------------------------------------------------------------------

    @Test
    void response204ReturnsNull() throws Exception {
        SynapseClient client = newTestClient(exchange -> {
            respondNoBody(exchange, 204);
        });

        Map<String, Object> result = client.request("DELETE", "/v1/contacts/u_1", null);
        assertNull(result);
    }
}
