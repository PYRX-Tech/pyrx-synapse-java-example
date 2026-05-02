package tech.pyrx.synapse.model;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class IdentifyBatchParams {

    private final List<IdentifyParams> contacts;
    private final String onConflict;

    public IdentifyBatchParams(List<IdentifyParams> contacts, String onConflict) {
        this.contacts = contacts;
        this.onConflict = onConflict;
    }

    public IdentifyBatchParams(List<IdentifyParams> contacts) {
        this(contacts, null);
    }

    public List<IdentifyParams> getContacts() { return contacts; }
    public String getOnConflict() { return onConflict; }

    public Map<String, Object> toMap() {
        Map<String, Object> map = new LinkedHashMap<>();
        List<Object> contactList = new ArrayList<>();
        for (IdentifyParams c : contacts) {
            contactList.add(c.toMap());
        }
        map.put("contacts", contactList);
        if (onConflict != null && !onConflict.isEmpty()) {
            map.put("on_conflict", onConflict);
        }
        return map;
    }
}
