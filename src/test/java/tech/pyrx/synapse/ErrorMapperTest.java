package tech.pyrx.synapse;

import org.junit.jupiter.api.Test;
import tech.pyrx.synapse.errors.*;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class ErrorMapperTest {

    // -----------------------------------------------------------------------
    // Error hierarchy (type checks)
    // -----------------------------------------------------------------------

    @Test
    void synapseErrorAttributes() {
        SynapseError err = new SynapseError("boom", 500, "internal", "req_1");
        assertEquals("boom", err.getMessage());
        assertEquals(500, err.getStatus());
        assertEquals("internal", err.getCode());
        assertEquals("req_1", err.getRequestId());
    }

    @Test
    void authErrorIsRuntimeException() {
        SynapseAuthError err = new SynapseAuthError("auth", 401, "", "");
        assertInstanceOf(RuntimeException.class, err);
        assertInstanceOf(SynapseError.class, err);
        assertEquals("auth", err.getMessage());
        assertEquals(401, err.getStatus());
    }

    @Test
    void rateLimitErrorIsRuntimeException() {
        SynapseRateLimitError err = new SynapseRateLimitError("slow", 429, "", "", 60.0);
        assertInstanceOf(SynapseError.class, err);
        assertEquals("slow", err.getMessage());
        assertEquals(429, err.getStatus());
    }

    @Test
    void planLimitErrorIsRuntimeException() {
        SynapsePlanLimitError err = new SynapsePlanLimitError("limit", 403, "plan_limit_reached", "", "contacts", 1000, 1000, "starter");
        assertInstanceOf(SynapseError.class, err);
        assertEquals("limit", err.getMessage());
        assertEquals(403, err.getStatus());
    }

    @Test
    void validationErrorIsRuntimeException() {
        SynapseValidationError err = new SynapseValidationError("invalid", 422, "", "", List.of());
        assertInstanceOf(SynapseError.class, err);
        assertEquals("invalid", err.getMessage());
        assertEquals(422, err.getStatus());
    }

    // -----------------------------------------------------------------------
    // RateLimitError
    // -----------------------------------------------------------------------

    @Test
    void rateLimitDefaultRetryAfter() {
        SynapseError err = ErrorMapper.mapError(429, Map.of("message", "slow down"), 0);
        assertInstanceOf(SynapseRateLimitError.class, err);
        assertEquals(60.0, ((SynapseRateLimitError) err).getRetryAfter());
    }

    @Test
    void rateLimitCustomRetryAfter() {
        SynapseError err = ErrorMapper.mapError(429, Map.of("message", "slow down"), 120.0);
        assertInstanceOf(SynapseRateLimitError.class, err);
        assertEquals(120.0, ((SynapseRateLimitError) err).getRetryAfter());
    }

    // -----------------------------------------------------------------------
    // PlanLimitError
    // -----------------------------------------------------------------------

    @Test
    void planLimitErrorAttributes() {
        Map<String, Object> body = Map.of(
            "message", "limit reached",
            "code", "plan_limit_reached",
            "limit_type", "contacts",
            "current", 1000,
            "maximum", 1000,
            "plan", "starter"
        );
        SynapseError err = ErrorMapper.mapError(403, body, 0);
        assertInstanceOf(SynapsePlanLimitError.class, err);
        SynapsePlanLimitError ple = (SynapsePlanLimitError) err;
        assertEquals("contacts", ple.getLimitType());
        assertEquals(1000, ple.getCurrent());
        assertEquals(1000, ple.getMaximum());
        assertEquals("starter", ple.getPlan());
    }

    // -----------------------------------------------------------------------
    // ValidationError
    // -----------------------------------------------------------------------

    @Test
    void validationErrorWithErrors() {
        Map<String, Object> body = Map.of(
            "message", "Validation failed",
            "errors", List.of(
                Map.of("field", "email", "message", "is invalid"),
                Map.of("field", "name", "msg", "is required")
            )
        );
        SynapseError err = ErrorMapper.mapError(422, body, 0);
        assertInstanceOf(SynapseValidationError.class, err);
        SynapseValidationError ve = (SynapseValidationError) err;
        assertEquals(2, ve.getErrors().size());
        assertEquals("email", ve.getErrors().get(0).getField());
        assertEquals("is invalid", ve.getErrors().get(0).getMessage());
        assertEquals("is required", ve.getErrors().get(1).getMessage());
    }

    @Test
    void validationErrorEmptyErrors() {
        Map<String, Object> body = Map.of("message", "invalid");
        SynapseError err = ErrorMapper.mapError(422, body, 0);
        assertInstanceOf(SynapseValidationError.class, err);
        assertTrue(((SynapseValidationError) err).getErrors().isEmpty());
    }

    // -----------------------------------------------------------------------
    // MapError routing
    // -----------------------------------------------------------------------

    @Test
    void mapError429() {
        SynapseError err = ErrorMapper.mapError(429, Map.of("message", "too fast"), 30.0);
        assertInstanceOf(SynapseRateLimitError.class, err);
        assertEquals(30.0, ((SynapseRateLimitError) err).getRetryAfter());
        assertEquals("too fast", err.getMessage());
    }

    @Test
    void mapError429DefaultRetryAfter() {
        SynapseError err = ErrorMapper.mapError(429, Map.of("message", "too fast"), 0);
        assertEquals(60.0, ((SynapseRateLimitError) err).getRetryAfter());
    }

    @Test
    void mapError403PlanLimit() {
        Map<String, Object> body = Map.of(
            "message", "Contact limit reached",
            "code", "plan_limit_reached",
            "limit_type", "contacts",
            "current", 500,
            "maximum", 500,
            "plan", "free"
        );
        SynapseError err = ErrorMapper.mapError(403, body, 0);
        assertInstanceOf(SynapsePlanLimitError.class, err);
        SynapsePlanLimitError ple = (SynapsePlanLimitError) err;
        assertEquals("contacts", ple.getLimitType());
        assertEquals(500, ple.getCurrent());
        assertEquals(500, ple.getMaximum());
        assertEquals("free", ple.getPlan());
    }

    @Test
    void mapError403WithoutPlanLimit() {
        SynapseError err = ErrorMapper.mapError(403, Map.of("message", "forbidden"), 0);
        assertInstanceOf(SynapseAuthError.class, err);
    }

    @Test
    void mapError401() {
        SynapseError err = ErrorMapper.mapError(401, Map.of("message", "unauthorized"), 0);
        assertInstanceOf(SynapseAuthError.class, err);
        assertEquals("unauthorized", err.getMessage());
    }

    @Test
    void mapError422() {
        Map<String, Object> body = Map.of(
            "message", "Validation failed",
            "errors", List.of(
                Map.of("field", "email", "message", "is invalid"),
                Map.of("field", "name", "msg", "is required")
            )
        );
        SynapseError err = ErrorMapper.mapError(422, body, 0);
        assertInstanceOf(SynapseValidationError.class, err);
        assertEquals(2, ((SynapseValidationError) err).getErrors().size());
    }

    @Test
    void mapErrorOtherStatus() {
        SynapseError err = ErrorMapper.mapError(500, Map.of("detail", "server error"), 0);
        assertNotNull(err);
        // Should NOT be an auth error
        assertFalse(err instanceof SynapseAuthError);
        assertEquals("server error", err.getMessage());
    }

    @Test
    void mapErrorDetailOverMessage() {
        SynapseError err = ErrorMapper.mapError(500, Map.of("detail", "detail msg", "message", "message msg"), 0);
        assertEquals("detail msg", err.getMessage());
    }

    @Test
    void mapErrorFallbackToHttpStatus() {
        SynapseError err = ErrorMapper.mapError(503, Map.of(), 0);
        assertEquals("HTTP 503", err.getMessage());
    }

    @Test
    void mapErrorExtractsCodeAndRequestId() {
        Map<String, Object> body = Map.of(
            "message", "err",
            "code", "some_code",
            "request_id", "req_abc"
        );
        SynapseError err = ErrorMapper.mapError(400, body, 0);
        assertEquals("some_code", err.getCode());
        assertEquals("req_abc", err.getRequestId());
    }
}
