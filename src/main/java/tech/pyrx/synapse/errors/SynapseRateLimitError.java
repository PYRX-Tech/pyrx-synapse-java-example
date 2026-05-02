package tech.pyrx.synapse.errors;

/**
 * Raised on 429 Too Many Requests.
 */
public class SynapseRateLimitError extends SynapseError {

    private final double retryAfter;

    public SynapseRateLimitError(String message, int status, String code, String requestId, double retryAfter) {
        super(message, status, code, requestId);
        this.retryAfter = retryAfter;
    }

    public double getRetryAfter() {
        return retryAfter;
    }
}
