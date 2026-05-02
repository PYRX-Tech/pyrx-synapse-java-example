package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactResponse {

    private final String id;
    private final String externalId;
    private final String email;
    private final String phone;
    private final String firstName;
    private final String lastName;
    private final String timezone;
    private final String locale;
    private final Map<String, Object> properties;
    private final List<String> tags;
    private final String createdAt;
    private final String updatedAt;

    public ContactResponse(String id, String externalId, String email, String phone,
                           String firstName, String lastName, String timezone, String locale,
                           Map<String, Object> properties, List<String> tags,
                           String createdAt, String updatedAt) {
        this.id = id;
        this.externalId = externalId;
        this.email = email;
        this.phone = phone;
        this.firstName = firstName;
        this.lastName = lastName;
        this.timezone = timezone;
        this.locale = locale;
        this.properties = properties;
        this.tags = tags;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }

    public String getId() { return id; }
    public String getExternalId() { return externalId; }
    public String getEmail() { return email; }
    public String getPhone() { return phone; }
    public String getFirstName() { return firstName; }
    public String getLastName() { return lastName; }
    public String getTimezone() { return timezone; }
    public String getLocale() { return locale; }
    public Map<String, Object> getProperties() { return properties; }
    public List<String> getTags() { return tags; }
    public String getCreatedAt() { return createdAt; }
    public String getUpdatedAt() { return updatedAt; }

    @SuppressWarnings("unchecked")
    public static ContactResponse fromMap(Map<String, Object> map) {
        List<String> tags = new ArrayList<>();
        List<Object> rawTags = JsonUtil.listFromMap(map, "tags");
        if (rawTags != null) {
            for (Object tag : rawTags) {
                if (tag instanceof String) {
                    tags.add((String) tag);
                }
            }
        }

        return new ContactResponse(
            JsonUtil.stringFromMap(map, "id"),
            JsonUtil.stringFromMap(map, "external_id"),
            JsonUtil.stringFromMap(map, "email"),
            JsonUtil.stringFromMap(map, "phone"),
            JsonUtil.stringFromMap(map, "first_name"),
            JsonUtil.stringFromMap(map, "last_name"),
            JsonUtil.stringFromMap(map, "timezone"),
            JsonUtil.stringFromMap(map, "locale"),
            JsonUtil.mapFromMap(map, "properties"),
            tags,
            JsonUtil.stringFromMap(map, "created_at"),
            JsonUtil.stringFromMap(map, "updated_at")
        );
    }
}
