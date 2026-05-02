package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class ContactListResponse {

    private final List<ContactResponse> data;
    private final ContactListMeta meta;

    public ContactListResponse(List<ContactResponse> data, ContactListMeta meta) {
        this.data = data;
        this.meta = meta;
    }

    public List<ContactResponse> getData() { return data; }
    public ContactListMeta getMeta() { return meta; }

    @SuppressWarnings("unchecked")
    public static ContactListResponse fromMap(Map<String, Object> map) {
        List<ContactResponse> data = new ArrayList<>();
        List<Object> rawData = JsonUtil.listFromMap(map, "data");
        if (rawData != null) {
            for (Object item : rawData) {
                if (item instanceof Map) {
                    data.add(ContactResponse.fromMap((Map<String, Object>) item));
                }
            }
        }

        ContactListMeta meta = null;
        Map<String, Object> rawMeta = JsonUtil.mapFromMap(map, "meta");
        if (rawMeta != null) {
            meta = ContactListMeta.fromMap(rawMeta);
        }

        return new ContactListResponse(data, meta);
    }
}
