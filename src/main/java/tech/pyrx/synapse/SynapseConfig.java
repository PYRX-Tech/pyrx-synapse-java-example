package tech.pyrx.synapse;

/**
 * Configuration builder for SynapseClient.
 */
public class SynapseConfig {

    private String apiKey;
    private String workspaceId;
    private String baseUrl = SynapseClient.DEFAULT_BASE_URL;
    private int timeoutSeconds = 30;
    private int maxRetries = 3;

    public SynapseConfig apiKey(String apiKey) {
        this.apiKey = apiKey;
        return this;
    }

    public SynapseConfig workspaceId(String workspaceId) {
        this.workspaceId = workspaceId;
        return this;
    }

    public SynapseConfig baseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
        return this;
    }

    public SynapseConfig timeoutSeconds(int timeoutSeconds) {
        this.timeoutSeconds = timeoutSeconds;
        return this;
    }

    public SynapseConfig maxRetries(int maxRetries) {
        this.maxRetries = maxRetries;
        return this;
    }

    public String getApiKey() { return apiKey; }
    public String getWorkspaceId() { return workspaceId; }
    public String getBaseUrl() { return baseUrl; }
    public int getTimeoutSeconds() { return timeoutSeconds; }
    public int getMaxRetries() { return maxRetries; }
}
