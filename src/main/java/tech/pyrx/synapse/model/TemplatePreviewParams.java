package tech.pyrx.synapse.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class TemplatePreviewParams {

    private final Map<String, Object> contact;
    private final Map<String, Object> triggerEvent;
    private final List<Map<String, Object>> additionalEvents;

    private TemplatePreviewParams(Builder builder) {
        this.contact = builder.contact;
        this.triggerEvent = builder.triggerEvent;
        this.additionalEvents = builder.additionalEvents;
    }

    public Map<String, Object> getContact() { return contact; }
    public Map<String, Object> getTriggerEvent() { return triggerEvent; }
    public List<Map<String, Object>> getAdditionalEvents() { return additionalEvents; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (contact != null && !contact.isEmpty()) map.put("contact", contact);
        if (triggerEvent != null && !triggerEvent.isEmpty()) map.put("trigger_event", triggerEvent);
        if (additionalEvents != null && !additionalEvents.isEmpty()) {
            map.put("additional_events", new ArrayList<>(additionalEvents));
        }
        return map;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Map<String, Object> contact;
        private Map<String, Object> triggerEvent;
        private List<Map<String, Object>> additionalEvents;

        public Builder contact(Map<String, Object> contact) { this.contact = contact; return this; }
        public Builder triggerEvent(Map<String, Object> triggerEvent) { this.triggerEvent = triggerEvent; return this; }
        public Builder additionalEvents(List<Map<String, Object>> additionalEvents) { this.additionalEvents = additionalEvents; return this; }
        public TemplatePreviewParams build() { return new TemplatePreviewParams(this); }
    }
}
