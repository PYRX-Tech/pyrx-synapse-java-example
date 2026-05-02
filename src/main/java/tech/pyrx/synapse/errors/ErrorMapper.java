package tech.pyrx.synapse.errors;

import tech.pyrx.synapse.internal.JsonUtil;
import tech.pyrx.synapse.model.ValidationFieldError;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Maps HTTP status codes and response bodies to the appropriate error type.
 */
public final class ErrorMapper {

    private ErrorMapper() {}

    public static SynapseError mapError(int status, Map<String, Object> body, double retryAfter) {
        if (body == null) {
            body = Map.of();
        }

        String message = extractMessage(body, status);
        String code = JsonUtil.stringFromMap(body, "code");
        String requestId = JsonUtil.stringFromMap(body, "request_id");

        if (status == 429) {
            double ra = retryAfter > 0 ? retryAfter : 60.0;
            return new SynapseRateLimitError(message, status, code, requestId, ra);
        }

        if (status == 403 && "plan_limit_reached".equals(code)) {
            return new SynapsePlanLimitError(
                message, status, code, requestId,
                JsonUtil.stringFromMap(body, "limit_type"),
                JsonUtil.intFromMap(body, "current"),
                JsonUtil.intFromMap(body, "maximum"),
                JsonUtil.stringFromMap(body, "plan")
            );
        }

        if (status == 401 || status == 403) {
            return new SynapseAuthError(message, status, code, requestId);
        }

        if (status == 422) {
            List<ValidationFieldError> fieldErrors = new ArrayList<>();
            List<Object> rawErrors = JsonUtil.listFromMap(body, "errors");
            if (rawErrors != null) {
                for (Object entry : rawErrors) {
                    if (entry instanceof Map) {
                        @SuppressWarnings("unchecked")
                        Map<String, Object> m = (Map<String, Object>) entry;
                        String msg = JsonUtil.stringFromMap(m, "message");
                        if (msg.isEmpty()) {
                            msg = JsonUtil.stringFromMap(m, "msg");
                        }
                        fieldErrors.add(new ValidationFieldError(
                            JsonUtil.stringFromMap(m, "field"),
                            msg
                        ));
                    }
                }
            }
            return new SynapseValidationError(message, status, code, requestId, fieldErrors);
        }

        return new SynapseError(message, status, code, requestId);
    }

    private static String extractMessage(Map<String, Object> body, int status) {
        Object detail = body.get("detail");
        if (detail instanceof String && !((String) detail).isEmpty()) {
            return (String) detail;
        }
        Object message = body.get("message");
        if (message instanceof String && !((String) message).isEmpty()) {
            return (String) message;
        }
        return "HTTP " + status;
    }
}
