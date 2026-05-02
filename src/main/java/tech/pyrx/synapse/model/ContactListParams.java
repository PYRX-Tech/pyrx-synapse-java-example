package tech.pyrx.synapse.model;

import java.util.LinkedHashMap;
import java.util.Map;

public class ContactListParams {

    private final String search;
    private final int page;
    private final int perPage;
    private final String sortBy;
    private final String sortOrder;

    private ContactListParams(Builder builder) {
        this.search = builder.search;
        this.page = builder.page;
        this.perPage = builder.perPage;
        this.sortBy = builder.sortBy;
        this.sortOrder = builder.sortOrder;
    }

    public String getSearch() { return search; }
    public int getPage() { return page; }
    public int getPerPage() { return perPage; }
    public String getSortBy() { return sortBy; }
    public String getSortOrder() { return sortOrder; }

    public Map<String, String> toQueryParams() {
        Map<String, String> params = new LinkedHashMap<>();
        if (search != null && !search.isEmpty()) params.put("search", search);
        if (page > 0) params.put("page", String.valueOf(page));
        if (perPage > 0) params.put("per_page", String.valueOf(perPage));
        if (sortBy != null && !sortBy.isEmpty()) params.put("sort_by", sortBy);
        if (sortOrder != null && !sortOrder.isEmpty()) params.put("sort_order", sortOrder);
        return params;
    }

    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private String search;
        private int page;
        private int perPage;
        private String sortBy;
        private String sortOrder;

        public Builder search(String search) { this.search = search; return this; }
        public Builder page(int page) { this.page = page; return this; }
        public Builder perPage(int perPage) { this.perPage = perPage; return this; }
        public Builder sortBy(String sortBy) { this.sortBy = sortBy; return this; }
        public Builder sortOrder(String sortOrder) { this.sortOrder = sortOrder; return this; }
        public ContactListParams build() { return new ContactListParams(this); }
    }
}
