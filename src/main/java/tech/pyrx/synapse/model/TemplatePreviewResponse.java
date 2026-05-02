package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class TemplatePreviewResponse {

    private final String subject;
    private final String html;
    private final boolean suppressed;
    private final String suppressedReason;

    public TemplatePreviewResponse(String subject, String html, boolean suppressed, String suppressedReason) {
        this.subject = subject;
        this.html = html;
        this.suppressed = suppressed;
        this.suppressedReason = suppressedReason;
    }

    public String getSubject() { return subject; }
    public String getHtml() { return html; }
    public boolean isSuppressed() { return suppressed; }
    public String getSuppressedReason() { return suppressedReason; }

    public static TemplatePreviewResponse fromMap(Map<String, Object> map) {
        return new TemplatePreviewResponse(
            JsonUtil.stringFromMap(map, "subject"),
            JsonUtil.stringFromMap(map, "html"),
            JsonUtil.boolFromMap(map, "suppressed"),
            JsonUtil.stringFromMap(map, "suppressed_reason")
        );
    }
}
