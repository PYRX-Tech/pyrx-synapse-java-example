# PYRX Synapse Java SDK

Official Java SDK for the [PYRX Synapse](https://synapse.pyrx.tech) customer engagement platform.

## Requirements

- Java 11+
- No runtime dependencies (uses `java.net.http.HttpClient`)

## Installation

### Maven

```xml
<dependency>
    <groupId>tech.pyrx</groupId>
    <artifactId>synapse</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'tech.pyrx:synapse:0.1.0'
```

## Quick Start

```java
import tech.pyrx.synapse.*;
import tech.pyrx.synapse.model.*;

SynapseClient client = new SynapseClient(
    new SynapseConfig()
        .apiKey("psk_live_your_api_key")
        .workspaceId("your_workspace_id")
);

// Track an event
TrackResponse response = client.track(
    TrackParams.builder()
        .externalId("user_123")
        .eventName("purchase")
        .attributes(Map.of("amount", 99.99, "currency", "USD"))
        .build()
);

// Identify a contact
ContactResponse contact = client.identify(
    IdentifyParams.builder()
        .externalId("user_123")
        .email("jane@example.com")
        .firstName("Jane")
        .lastName("Doe")
        .build()
);

// Send an email
SendResponse send = client.send(
    SendParams.builder()
        .templateSlug("welcome")
        .to(Map.of("email", "jane@example.com"))
        .attributes(Map.of("name", "Jane"))
        .build()
);
```

## Sub-clients

### Contacts

```java
// List contacts
ContactListResponse list = client.contacts.list(
    ContactListParams.builder().page(1).perPage(20).build()
);

// Get a contact
ContactResponse contact = client.contacts.get("contact_id");

// Update a contact
ContactResponse updated = client.contacts.update("external_id",
    ContactUpdateParams.builder().email("new@example.com").build()
);

// Delete a contact
client.contacts.delete("external_id");
```

### Templates

```java
// List templates
List<TemplateResponse> templates = client.templates.list();

// Get a template
TemplateResponse template = client.templates.get("welcome");

// Create a template
TemplateResponse created = client.templates.create(
    TemplateCreateParams.builder()
        .name("Welcome")
        .slug("welcome")
        .subject("Welcome!")
        .bodyHtml("<h1>Hello</h1>")
        .senderName("PYRX")
        .fromEmail("hello@example.com")
        .build()
);

// Preview a template
TemplatePreviewResponse preview = client.templates.preview("welcome",
    TemplatePreviewParams.builder()
        .contact(Map.of("first_name", "Jane"))
        .build()
);
```

## Webhook Verification

```java
import tech.pyrx.synapse.Webhooks;

Map<String, Object> event = Webhooks.verify(
    payload,
    Map.of(
        "svix-id", request.getHeader("svix-id"),
        "svix-timestamp", request.getHeader("svix-timestamp"),
        "svix-signature", request.getHeader("svix-signature")
    ),
    "whsec_your_webhook_secret"
);
```

## Error Handling

All API errors throw `SynapseError` (unchecked), with specific subtypes:

- `SynapseAuthError` -- 401/403
- `SynapseRateLimitError` -- 429 (includes `getRetryAfter()`)
- `SynapsePlanLimitError` -- 403 plan limit reached
- `SynapseValidationError` -- 422 (includes field errors)

```java
try {
    client.track(params);
} catch (SynapseRateLimitError e) {
    System.out.println("Retry after: " + e.getRetryAfter() + "s");
} catch (SynapseValidationError e) {
    e.getErrors().forEach(err ->
        System.out.println(err.getField() + ": " + err.getMessage()));
} catch (SynapseError e) {
    System.out.println("API error: " + e.getStatus() + " " + e.getMessage());
}
```

## Configuration

```java
SynapseClient client = new SynapseClient(
    new SynapseConfig()
        .apiKey("psk_live_...")
        .workspaceId("ws_...")
        .baseUrl("https://synapse-api.pyrx.tech")  // default
        .timeoutSeconds(30)                          // default
        .maxRetries(3)                               // default
);
```

## License

MIT
