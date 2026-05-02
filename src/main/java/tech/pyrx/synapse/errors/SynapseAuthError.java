package tech.pyrx.synapse.errors;

/**
 * Raised on 401 Unauthorized or 403 Forbidden.
 */
public class SynapseAuthError extends SynapseError {

    public SynapseAuthError(String message, int status, String code, String requestId) {
        super(message, status, code, requestId);
    }
}
