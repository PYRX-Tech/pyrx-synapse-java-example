package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class BulkContactResponse {

    private final int total;
    private final int created;
    private final int updated;
    private final int skipped;
    private final List<Map<String, Object>> errors;

    public BulkContactResponse(int total, int created, int updated, int skipped,
                                List<Map<String, Object>> errors) {
        this.total = total;
        this.created = created;
        this.updated = updated;
        this.skipped = skipped;
        this.errors = errors;
    }

    public int getTotal() { return total; }
    public int getCreated() { return created; }
    public int getUpdated() { return updated; }
    public int getSkipped() { return skipped; }
    public List<Map<String, Object>> getErrors() { return errors; }

    @SuppressWarnings("unchecked")
    public static BulkContactResponse fromMap(Map<String, Object> map) {
        List<Map<String, Object>> errors = new ArrayList<>();
        List<Object> rawErrors = JsonUtil.listFromMap(map, "errors");
        if (rawErrors != null) {
            for (Object item : rawErrors) {
                if (item instanceof Map) {
                    errors.add((Map<String, Object>) item);
                }
            }
        }

        return new BulkContactResponse(
            JsonUtil.intFromMap(map, "total"),
            JsonUtil.intFromMap(map, "created"),
            JsonUtil.intFromMap(map, "updated"),
            JsonUtil.intFromMap(map, "skipped"),
            errors
        );
    }
}
