package tech.pyrx.synapse.errors;

/**
 * Base exception for all Synapse API errors.
 */
public class SynapseError extends RuntimeException {

    private final int status;
    private final String code;
    private final String requestId;

    public SynapseError(String message, int status, String code, String requestId) {
        super(message);
        this.status = status;
        this.code = code;
        this.requestId = requestId;
    }

    public int getStatus() {
        return status;
    }

    public String getCode() {
        return code;
    }

    public String getRequestId() {
        return requestId;
    }
}
