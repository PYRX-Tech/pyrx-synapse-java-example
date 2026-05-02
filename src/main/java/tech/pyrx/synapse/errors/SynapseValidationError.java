package tech.pyrx.synapse.errors;

import tech.pyrx.synapse.model.ValidationFieldError;

import java.util.List;

/**
 * Raised on 422 Unprocessable Entity.
 */
public class SynapseValidationError extends SynapseError {

    private final List<ValidationFieldError> errors;

    public SynapseValidationError(String message, int status, String code, String requestId,
                                   List<ValidationFieldError> errors) {
        super(message, status, code, requestId);
        this.errors = errors;
    }

    public List<ValidationFieldError> getErrors() {
        return errors;
    }
}
