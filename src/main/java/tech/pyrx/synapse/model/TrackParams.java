package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class TrackParams {

    private final String externalId;
    private final String eventName;
    private final Map<String, Object> attributes;
    private final Map<String, Object> contact;
    private final String idempotencyKey;
    private final String occurredAt;

    private TrackParams(Builder builder) {
        this.externalId = builder.externalId;
        this.eventName = builder.eventName;
        this.attributes = builder.attributes;
        this.contact = builder.contact;
        this.idempotencyKey = builder.idempotencyKey;
        this.occurredAt = builder.occurredAt;
    }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("external_id", externalId);
        map.put("event_name", eventName);
        if (attributes != null && !attributes.isEmpty()) {
            map.put("attributes", attributes);
        }
        if (contact != null && !contact.isEmpty()) {
            map.put("contact", contact);
        }
        if (idempotencyKey != null && !idempotencyKey.isEmpty()) {
            map.put("idempotency_key", idempotencyKey);
        }
        if (occurredAt != null && !occurredAt.isEmpty()) {
            map.put("occurred_at", occurredAt);
        }
        return map;
    }

    public String getExternalId() { return externalId; }
    public String getEventName() { return eventName; }
    public Map<String, Object> getAttributes() { return attributes; }
    public Map<String, Object> getContact() { return contact; }
    public String getIdempotencyKey() { return idempotencyKey; }
    public String getOccurredAt() { return occurredAt; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String externalId;
        private String eventName;
        private Map<String, Object> attributes;
        private Map<String, Object> contact;
        private String idempotencyKey;
        private String occurredAt;

        public Builder externalId(String externalId) { this.externalId = externalId; return this; }
        public Builder eventName(String eventName) { this.eventName = eventName; return this; }
        public Builder attributes(Map<String, Object> attributes) { this.attributes = attributes; return this; }
        public Builder contact(Map<String, Object> contact) { this.contact = contact; return this; }
        public Builder idempotencyKey(String idempotencyKey) { this.idempotencyKey = idempotencyKey; return this; }
        public Builder occurredAt(String occurredAt) { this.occurredAt = occurredAt; return this; }
        public TrackParams build() { return new TrackParams(this); }
    }
}
