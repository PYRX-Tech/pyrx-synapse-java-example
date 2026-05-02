package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class TemplateUpdateParams {

    private final String name;
    private final String subject;
    private final String bodyHtml;
    private final String senderName;
    private final String fromEmail;

    private TemplateUpdateParams(Builder builder) {
        this.name = builder.name;
        this.subject = builder.subject;
        this.bodyHtml = builder.bodyHtml;
        this.senderName = builder.senderName;
        this.fromEmail = builder.fromEmail;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (name != null && !name.isEmpty()) map.put("name", name);
        if (subject != null && !subject.isEmpty()) map.put("subject", subject);
        if (bodyHtml != null && !bodyHtml.isEmpty()) map.put("body_html", bodyHtml);
        if (senderName != null && !senderName.isEmpty()) map.put("sender_name", senderName);
        if (fromEmail != null && !fromEmail.isEmpty()) map.put("from_email", fromEmail);
        return map;
    }

    public String getName() { return name; }
    public String getSubject() { return subject; }
    public String getBodyHtml() { return bodyHtml; }
    public String getSenderName() { return senderName; }
    public String getFromEmail() { return fromEmail; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String name;
        private String subject;
        private String bodyHtml;
        private String senderName;
        private String fromEmail;

        public Builder name(String name) { this.name = name; return this; }
        public Builder subject(String subject) { this.subject = subject; return this; }
        public Builder bodyHtml(String bodyHtml) { this.bodyHtml = bodyHtml; return this; }
        public Builder senderName(String senderName) { this.senderName = senderName; return this; }
        public Builder fromEmail(String fromEmail) { this.fromEmail = fromEmail; return this; }
        public TemplateUpdateParams build() { return new TemplateUpdateParams(this); }
    }
}
