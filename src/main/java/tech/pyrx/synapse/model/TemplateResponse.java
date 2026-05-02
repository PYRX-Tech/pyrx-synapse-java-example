package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class TemplateResponse {

    private final String id;
    private final String name;
    private final String slug;
    private final String subject;
    private final String bodyHtml;
    private final String senderName;
    private final String fromEmail;
    private final String replyTo;
    private final String contentType;
    private final String createdAt;
    private final String updatedAt;

    public TemplateResponse(String id, String name, String slug, String subject,
                            String bodyHtml, String senderName, String fromEmail,
                            String replyTo, String contentType,
                            String createdAt, String updatedAt) {
        this.id = id;
        this.name = name;
        this.slug = slug;
        this.subject = subject;
        this.bodyHtml = bodyHtml;
        this.senderName = senderName;
        this.fromEmail = fromEmail;
        this.replyTo = replyTo;
        this.contentType = contentType;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getSubject() { return subject; }
    public String getBodyHtml() { return bodyHtml; }
    public String getSenderName() { return senderName; }
    public String getFromEmail() { return fromEmail; }
    public String getReplyTo() { return replyTo; }
    public String getContentType() { return contentType; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    public static TemplateResponse fromMap(Map<String, Object> map) {
        return new TemplateResponse(
            JsonUtil.stringFromMap(map, "id"),
            JsonUtil.stringFromMap(map, "name"),
            JsonUtil.stringFromMap(map, "slug"),
            JsonUtil.stringFromMap(map, "subject"),
            JsonUtil.stringFromMap(map, "body_html"),
            JsonUtil.stringFromMap(map, "sender_name"),
            JsonUtil.stringFromMap(map, "from_email"),
            JsonUtil.stringFromMap(map, "reply_to"),
            JsonUtil.stringFromMap(map, "content_type"),
            JsonUtil.stringFromMap(map, "created_at"),
            JsonUtil.stringFromMap(map, "updated_at")
        );
    }
}
