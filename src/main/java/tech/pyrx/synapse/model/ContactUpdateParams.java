package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class ContactUpdateParams {

    private final String email;
    private final Map<String, Object> properties;
    private final List<String> tags;
    private final List<String> addTags;
    private final List<String> removeTags;

    private ContactUpdateParams(Builder builder) {
        this.email = builder.email;
        this.properties = builder.properties;
        this.tags = builder.tags;
        this.addTags = builder.addTags;
        this.removeTags = builder.removeTags;
    }

    public String getEmail() { return email; }
    public Map<String, Object> getProperties() { return properties; }
    public List<String> getTags() { return tags; }
    public List<String> getAddTags() { return addTags; }
    public List<String> getRemoveTags() { return removeTags; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        if (email != null && !email.isEmpty()) map.put("email", email);
        if (properties != null && !properties.isEmpty()) map.put("properties", properties);
        if (tags != null && !tags.isEmpty()) map.put("tags", tags);
        if (addTags != null && !addTags.isEmpty()) map.put("add_tags", addTags);
        if (removeTags != null && !removeTags.isEmpty()) map.put("remove_tags", removeTags);
        return map;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String email;
        private Map<String, Object> properties;
        private List<String> tags;
        private List<String> addTags;
        private List<String> removeTags;

        public Builder email(String email) { this.email = email; return this; }
        public Builder properties(Map<String, Object> properties) { this.properties = properties; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public Builder addTags(List<String> addTags) { this.addTags = addTags; return this; }
        public Builder removeTags(List<String> removeTags) { this.removeTags = removeTags; return this; }
        public ContactUpdateParams build() { return new ContactUpdateParams(this); }
    }
}
