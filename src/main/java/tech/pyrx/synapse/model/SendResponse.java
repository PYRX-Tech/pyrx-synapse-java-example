package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class SendResponse {

    private final String emailLogId;
    private final String status;

    public SendResponse(String emailLogId, String status) {
        this.emailLogId = emailLogId;
        this.status = status;
    }

    public String getEmailLogId() { return emailLogId; }
    public String getStatus() { return status; }

    public static SendResponse fromMap(Map<String, Object> map) {
        return new SendResponse(
            JsonUtil.stringFromMap(map, "email_log_id"),
            JsonUtil.stringFromMap(map, "status")
        );
    }
}
