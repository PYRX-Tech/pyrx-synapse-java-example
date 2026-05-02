package tech.pyrx.synapse;

import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.SecureRandom;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class WebhooksTest {

    private static byte[] generateSecretBytes() {
        byte[] raw = new byte[32];
        new SecureRandom().nextBytes(raw);
        return raw;
    }

    private static String toBase64(byte[] raw) {
        return Base64.getEncoder().encodeToString(raw);
    }

    private static String toWhsec(byte[] raw) {
        return "whsec_" + toBase64(raw);
    }

    private static String computeSignature(byte[] secretBytes, String msgId, String timestamp, String payload) {
        try {
            String signedContent = msgId + "." + timestamp + "." + payload;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            byte[] sig = mac.doFinal(signedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return "v1," + Base64.getEncoder().encodeToString(sig);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // -----------------------------------------------------------------------
    // Valid signatures
    // -----------------------------------------------------------------------

    @Test
    void validWithPrefix() {
        byte[] secretRaw = generateSecretBytes();
        String secretWithPrefix = toWhsec(secretRaw);
        String payload = "{\"type\":\"contact.created\",\"data\":{\"id\":\"c_123\"}}";
        String svixId = "msg_abc123";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = computeSignature(secretRaw, svixId, svixTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", sig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, secretWithPrefix, false);
        assertEquals("contact.created", result.get("type"));
    }

    @Test
    void validWithoutPrefix() {
        byte[] secretRaw = generateSecretBytes();
        String secretB64 = toBase64(secretRaw);
        String payload = "{\"type\":\"contact.created\",\"data\":{\"id\":\"c_123\"}}";
        String svixId = "msg_abc123";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = computeSignature(secretRaw, svixId, svixTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", sig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, secretB64, false);
        assertEquals("contact.created", result.get("type"));
    }

    // -----------------------------------------------------------------------
    // Missing headers
    // -----------------------------------------------------------------------

    @Test
    void missingSvixId() {
        byte[] secretRaw = generateSecretBytes();
        Map<String, String> headers = Map.of(
            "svix-timestamp", "123",
            "svix-signature", "v1,abc"
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify("{}", headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Missing required webhook headers"));
    }

    @Test
    void missingSvixTimestamp() {
        byte[] secretRaw = generateSecretBytes();
        Map<String, String> headers = Map.of(
            "svix-id", "msg_1",
            "svix-signature", "v1,abc"
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify("{}", headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Missing required webhook headers"));
    }

    @Test
    void missingSvixSignature() {
        byte[] secretRaw = generateSecretBytes();
        Map<String, String> headers = Map.of(
            "svix-id", "msg_1",
            "svix-timestamp", "123"
        );
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify("{}", headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Missing required webhook headers"));
    }

    @Test
    void emptyHeaders() {
        byte[] secretRaw = generateSecretBytes();
        Map<String, String> headers = new HashMap<>();
        headers.put("svix-id", "");
        headers.put("svix-timestamp", "123");
        headers.put("svix-signature", "v1,abc");
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify("{}", headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Missing required webhook headers"));
    }

    // -----------------------------------------------------------------------
    // Expired timestamp
    // -----------------------------------------------------------------------

    @Test
    void expiredTimestamp() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String oldTimestamp = String.valueOf(System.currentTimeMillis() / 1000 - 600);
        String sig = computeSignature(secretRaw, svixId, oldTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", oldTimestamp,
            "svix-signature", sig
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify(payload, headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Webhook timestamp too old"));
    }

    @Test
    void disabledTimestampCheck() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String oldTimestamp = String.valueOf(System.currentTimeMillis() / 1000 - 600);
        String sig = computeSignature(secretRaw, svixId, oldTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", oldTimestamp,
            "svix-signature", sig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, toWhsec(secretRaw), true);
        assertEquals("test", result.get("type"));
    }

    // -----------------------------------------------------------------------
    // Invalid signature
    // -----------------------------------------------------------------------

    @Test
    void invalidSignature() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", "v1,aW52YWxpZHNpZ25hdHVyZWhlcmU="
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify(payload, headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Webhook signature verification failed"));
    }

    // -----------------------------------------------------------------------
    // Multiple signatures (key rotation)
    // -----------------------------------------------------------------------

    @Test
    void multipleSignaturesOneValid() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String validSig = computeSignature(secretRaw, svixId, svixTimestamp, payload);
        String badSig = "v1,aW52YWxpZHNpZ25hdHVyZWhlcmU=";
        String multiSig = badSig + " " + validSig;

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", multiSig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, toWhsec(secretRaw), false);
        assertEquals("test", result.get("type"));
    }

    @Test
    void multipleSignaturesNoneValid() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String multiSig = "v1,aW52YWxpZA== v1,YW5vdGhlcmludmFsaWQ=";

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", multiSig
        );

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class, () ->
            Webhooks.verify(payload, headers, toWhsec(secretRaw), false));
        assertTrue(ex.getMessage().contains("Webhook signature verification failed"));
    }

    // -----------------------------------------------------------------------
    // whsec_ prefix stripping
    // -----------------------------------------------------------------------

    @Test
    void secretWithPrefix() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = computeSignature(secretRaw, svixId, svixTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", sig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, toWhsec(secretRaw), false);
        assertNotNull(result.get("type"));
    }

    @Test
    void secretWithoutPrefix() {
        byte[] secretRaw = generateSecretBytes();
        String payload = "{\"type\":\"test\"}";
        String svixId = "msg_1";
        String svixTimestamp = String.valueOf(System.currentTimeMillis() / 1000);
        String sig = computeSignature(secretRaw, svixId, svixTimestamp, payload);

        Map<String, String> headers = Map.of(
            "svix-id", svixId,
            "svix-timestamp", svixTimestamp,
            "svix-signature", sig
        );

        Map<String, Object> result = Webhooks.verify(payload, headers, toBase64(secretRaw), false);
        assertNotNull(result.get("type"));
    }
}
