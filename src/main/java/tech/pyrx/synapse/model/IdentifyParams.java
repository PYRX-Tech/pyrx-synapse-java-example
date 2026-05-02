package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IdentifyParams {

    private final String externalId;
    private final String email;
    private final String phone;
    private final String firstName;
    private final String lastName;
    private final String timezone;
    private final String locale;
    private final Map<String, Object> properties;
    private final List<String> tags;

    private IdentifyParams(Builder builder) {
        this.externalId = builder.externalId;
        this.email = builder.email;
        this.phone = builder.phone;
        this.firstName = builder.firstName;
        this.lastName = builder.lastName;
        this.timezone = builder.timezone;
        this.locale = builder.locale;
        this.properties = builder.properties;
        this.tags = builder.tags;
    }

    @SuppressWarnings("unchecked")
    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        map.put("external_id", externalId);
        if (email != null && !email.isEmpty()) map.put("email", email);
        if (phone != null && !phone.isEmpty()) map.put("phone", phone);
        if (firstName != null && !firstName.isEmpty()) map.put("first_name", firstName);
        if (lastName != null && !lastName.isEmpty()) map.put("last_name", lastName);
        if (timezone != null && !timezone.isEmpty()) map.put("timezone", timezone);
        if (locale != null && !locale.isEmpty()) map.put("locale", locale);
        if (properties != null && !properties.isEmpty()) map.put("properties", properties);
        if (tags != null && !tags.isEmpty()) map.put("tags", tags);
        return map;
    }

    public String getExternalId() { return externalId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getTimezone() { return timezone; }
    public String getLocale() { return locale; }
    public Map<String, Object> getProperties() { return properties; }
    public List<String> getTags() { return tags; }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String externalId;
        private String email;
        private String phone;
        private String firstName;
        private String lastName;
        private String timezone;
        private String locale;
        private Map<String, Object> properties;
        private List<String> tags;

        public Builder externalId(String externalId) { this.externalId = externalId; return this; }
        public Builder email(String email) { this.email = email; return this; }
        public Builder phone(String phone) { this.phone = phone; return this; }
        public Builder firstName(String firstName) { this.firstName = firstName; return this; }
        public Builder lastName(String lastName) { this.lastName = lastName; return this; }
        public Builder timezone(String timezone) { this.timezone = timezone; return this; }
        public Builder locale(String locale) { this.locale = locale; return this; }
        public Builder properties(Map<String, Object> properties) { this.properties = properties; return this; }
        public Builder tags(List<String> tags) { this.tags = tags; return this; }
        public IdentifyParams build() { return new IdentifyParams(this); }
    }
}
