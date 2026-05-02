package tech.pyrx.synapse;

import tech.pyrx.synapse.model.*;

import java.util.Map;

/**
 * Sub-client for managing contacts.
 */
public class ContactsClient {

    private final SynapseClient client;

    ContactsClient(SynapseClient client) {
        this.client = client;
    }

    /**
     * Returns a paginated list of contacts.
     */
    public ContactListResponse list(ContactListParams params) {
        Map<String, Object> resp = client.requestWithParams("GET", "/v1/contacts", null, params.toQueryParams());
        return ContactListResponse.fromMap(resp);
    }

    /**
     * Retrieves a single contact by ID.
     */
    public ContactResponse get(String contactId) {
        Map<String, Object> resp = client.request("GET", "/v1/contacts/" + contactId, null);
        return ContactResponse.fromMap(resp);
    }

    /**
     * Updates a contact by external ID.
     */
    public ContactResponse update(String externalId, ContactUpdateParams params) {
        Map<String, Object> resp = client.requestWithParams("PATCH", "/v1/contacts/" + externalId,
            params.toMap(), null);
        return ContactResponse.fromMap(resp);
    }

    /**
     * Deletes a contact by external ID.
     */
    public void delete(String externalId) {
        client.request("DELETE", "/v1/contacts/" + externalId, null);
    }
}
