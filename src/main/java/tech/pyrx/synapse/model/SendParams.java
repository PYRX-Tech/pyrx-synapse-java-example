package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class SendParams {

    private final String templateSlug;
    private final Map<String, Object> to;
    private final Map<String, Object> attributes;
    private final String idempotencyKey;

    private SendParams(Builder builder) {
        this.templateSlug = builder.templateSlug;
        this.to = builder.to;
        this.attributes = builder.attributes;
        this.idempotencyKey = builder.idempotencyKey;
    }

    public String getTemplateSlug() { return templateSlug; }
    public Map<String, Object> getTo() { return to; }
    public Map<String, Object> getAttributes() { return attributes; }
    public String getIdempotencyKey() { return idempotencyKey; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("template_slug", templateSlug);
        map.put("to", to);
        if (attributes != null && !attributes.isEmpty()) {
            map.put("attributes", attributes);
        }
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            map.put("idempotency_key", idempotencyKey);
        }
        return map;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String templateSlug;
        private Map<String, Object> to;
        private Map<String, Object> attributes;
        private String idempotencyKey;

        public Builder templateSlug(String templateSlug) { this.templateSlug = templateSlug; return this; }
        public Builder to(Map<String, Object> to) { this.to = to; return this; }
        public Builder attributes(Map<String, Object> attributes) { this.attributes = attributes; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public SendParams build() { return new SendParams(this); }
    }
}
