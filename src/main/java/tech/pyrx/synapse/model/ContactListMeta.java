package tech.pyrx.synapse.model;

import tech.pyrx.synapse.internal.JsonUtil;

import java.util.Map;

public class ContactListMeta {

    private final int total;
    private final int page;
    private final int perPage;
    private final int totalPages;

    public ContactListMeta(int total, int page, int perPage, int totalPages) {
        this.total = total;
        this.page = page;
        this.perPage = perPage;
        this.totalPages = totalPages;
    }

    public int getTotal() { return total; }
    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    public int getTotalPages() { return totalPages; }

    public static ContactListMeta fromMap(Map<String, Object> map) {
        return new ContactListMeta(
            JsonUtil.intFromMap(map, "total"),
            JsonUtil.intFromMap(map, "page"),
            JsonUtil.intFromMap(map, "per_page"),
            JsonUtil.intFromMap(map, "total_pages")
        );
    }
}
