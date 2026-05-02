package tech.pyrx.synapse.model;

/**
 * Represents a single field validation error.
 */
public class ValidationFieldError {

    private final String field;
    private final String message;

    public ValidationFieldError(String field, String message) {
        this.field = field;
        this.message = message;
    }

    public String getField() { return field; }
    public String getMessage() { return message; }
}
