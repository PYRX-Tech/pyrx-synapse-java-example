package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class TemplateCreateParams {

    private final String name;
    private final String slug;
    private final String subject;
    private final String bodyHtml;
    private final String senderName;
    private final String fromEmail;
    private final String replyTo;

    private TemplateCreateParams(Builder builder) {
        this.name = builder.name;
        this.slug = builder.slug;
        this.subject = builder.subject;
        this.bodyHtml = builder.bodyHtml;
        this.senderName = builder.senderName;
        this.fromEmail = builder.fromEmail;
        this.replyTo = builder.replyTo;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("name", name);
        map.put("slug", slug);
        map.put("subject", subject);
        map.put("body_html", bodyHtml);
        map.put("sender_name", senderName);
        map.put("from_email", fromEmail);
        if (replyTo != null && !replyTo.isEmpty()) {
            map.put("reply_to", replyTo);
        }
        return map;
    }

    public String getName() { return name; }
    public String getSlug() { return slug; }
    public String getSubject() { return subject; }
    public String getBodyHtml() { return bodyHtml; }
    public String getSenderName() { return senderName; }
    public String getFromEmail() { return fromEmail; }
    public String getReplyTo() { return replyTo; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String slug;
        private String subject;
        private String bodyHtml;
        private String senderName;
        private String fromEmail;
        private String replyTo;

        public Builder name(String name) { this.name = name; return this; }
        public Builder slug(String slug) { this.slug = slug; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder fromEmail(String fromEmail) { this.fromEmail = fromEmail; return this; }
        public Builder replyTo(String replyTo) { this.replyTo = replyTo; return this; }
        public TemplateCreateParams build() { return new TemplateCreateParams(this); }
    }
}
