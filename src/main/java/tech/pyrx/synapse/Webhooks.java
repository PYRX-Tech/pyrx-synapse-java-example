package tech.pyrx.synapse;

import tech.pyrx.synapse.internal.JsonUtil;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.security.MessageDigest;
import java.util.Base64;
import java.util.Map;

/**
 * Webhook signature verification using HMAC-SHA256 (Svix format).
 */
public final class Webhooks {

    private static final int TOLERANCE_SECONDS = 300; // 5 minutes

    private Webhooks() {}

    /**
     * Verifies a webhook signature and returns the parsed payload.
     */
    public static Map<String, Object> verify(String payload, Map<String, String> headers, String secret) {
        return verify(payload, headers, secret, false);
    }

    /**
     * Verifies a webhook signature and returns the parsed payload.
     * Set disableTimestampCheck to true to skip timestamp tolerance validation (testing only).
     */
    public static Map<String, Object> verify(String payload, Map<String, String> headers, String secret,
                                              boolean disableTimestampCheck) {
        String svixId = headers.getOrDefault("svix-id", "");
        String svixTimestamp = headers.getOrDefault("svix-timestamp", "");
        String svixSignature = headers.getOrDefault("svix-signature", "");

        if (svixId.isEmpty() || svixTimestamp.isEmpty() || svixSignature.isEmpty()) {
            throw new IllegalArgumentException("Missing required webhook headers: svix-id, svix-timestamp, svix-signature");
        }

        // Validate timestamp
        if (!disableTimestampCheck) {
            long ts;
            try {
                ts = Long.parseLong(svixTimestamp);
            } catch (NumberFormatException e) {
                throw new IllegalArgumentException("Invalid svix-timestamp header");
            }
            long nowSec = System.currentTimeMillis() / 1000;
            long diff = Math.abs(nowSec - ts);
            if (diff > TOLERANCE_SECONDS) {
                throw new IllegalArgumentException(
                    String.format("Webhook timestamp too old (%ds > %ds tolerance)", diff, TOLERANCE_SECONDS));
            }
        }

        // Decode secret: strip "whsec_" prefix and base64-decode
        String secretRaw = secret;
        if (secretRaw.startsWith("whsec_")) {
            secretRaw = secretRaw.substring(6);
        }
        byte[] secretBytes;
        try {
            secretBytes = Base64.getDecoder().decode(secretRaw);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Failed to decode webhook secret: " + e.getMessage());
        }

        // Compute expected signature
        String signedContent = svixId + "." + svixTimestamp + "." + payload;
        byte[] expectedBytes;
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secretBytes, "HmacSHA256"));
            expectedBytes = mac.doFinal(signedContent.getBytes(java.nio.charset.StandardCharsets.UTF_8));
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC: " + e.getMessage(), e);
        }

        // Compare against all provided signatures (svix sends multiple for key rotation)
        String[] signatures = svixSignature.split(" ");
        boolean verified = false;

        for (String sig : signatures) {
            String[] parts = sig.split(",", 2);
            if (parts.length < 2) continue;
            byte[] sigBytes;
            try {
                sigBytes = Base64.getDecoder().decode(parts[1]);
            } catch (IllegalArgumentException e) {
                continue;
            }
            if (MessageDigest.isEqual(sigBytes, expectedBytes)) {
                verified = true;
                break;
            }
        }

        if (!verified) {
            throw new IllegalArgumentException("Webhook signature verification failed");
        }

        // Parse and return the event
        try {
            return JsonUtil.deserializeObject(payload);
        } catch (Exception e) {
            throw new IllegalArgumentException("Failed to parse webhook payload as JSON");
        }
    }
}
