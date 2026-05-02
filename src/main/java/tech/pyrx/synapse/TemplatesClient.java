package tech.pyrx.synapse;

import tech.pyrx.synapse.internal.JsonUtil;
import tech.pyrx.synapse.model.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Sub-client for managing email templates.
 */
public class TemplatesClient {

    private final SynapseClient client;

    TemplatesClient(SynapseClient client) {
        this.client = client;
    }

    /**
     * Returns all email templates.
     */
    @SuppressWarnings("unchecked")
    public List<TemplateResponse> list() {
        Map<String, Object> resp = client.request("GET", "/v1/templates", null);
        List<TemplateResponse> result = new ArrayList<>();
        // The response is an array wrapped in a synthetic "_array" key
        Object arr = resp.get("_array");
        if (arr instanceof List) {
            for (Object item : (List<Object>) arr) {
                if (item instanceof Map) {
                    result.add(TemplateResponse.fromMap((Map<String, Object>) item));
                }
            }
        }
        return result;
    }

    /**
     * Retrieves a single template by slug.
     */
    public TemplateResponse get(String slug) {
        Map<String, Object> resp = client.request("GET", "/v1/templates/" + slug, null);
        return TemplateResponse.fromMap(resp);
    }

    /**
     * Creates a new email template.
     */
    public TemplateResponse create(TemplateCreateParams params) {
        Map<String, Object> resp = client.request("POST", "/v1/templates", params.toMap());
        return TemplateResponse.fromMap(resp);
    }

    /**
     * Updates an existing template by slug.
     */
    public TemplateResponse update(String slug, TemplateUpdateParams params) {
        Map<String, Object> resp = client.request("PUT", "/v1/templates/" + slug, params.toMap());
        return TemplateResponse.fromMap(resp);
    }

    /**
     * Deletes a template by slug.
     */
    public void delete(String slug) {
        client.request("DELETE", "/v1/templates/" + slug, null);
    }

    /**
     * Previews a template with sample data.
     */
    public TemplatePreviewResponse preview(String slug, TemplatePreviewParams params) {
        Map<String, Object> resp = client.request("POST", "/v1/templates/" + slug + "/preview", params.toMap());
        return TemplatePreviewResponse.fromMap(resp);
    }
}
