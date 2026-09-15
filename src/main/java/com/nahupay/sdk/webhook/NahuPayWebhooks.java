package com.nahupay.sdk.webhook;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nahupay.sdk.exception.NahuPayWebhookException;
import com.nahupay.sdk.model.WebhookEvent;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;

/**
 * Webhook signature verification utilities.
 *
 * <p><b>How NahuPay signs webhooks:</b><br>
 * The backend computes {@code HMAC-SHA256(rawBody, secretKey)} and sends the
 * result in the {@code X-NahuPay-Signature} header as {@code sha256=<hex-digest>}.
 * The {@code secretKey} is the merchant's <em>secret</em> API key for the payment
 * mode ({@code sk_test_…} for TEST payments, {@code sk_live_…} for LIVE payments).
 *
 * <p><b>Spring Boot / Servlet setup:</b><br>
 * You <em>must</em> receive the raw request body — not the parsed JSON — or the
 * signature will not match. Use {@code HttpServletRequest.getInputStream()} or
 * Spring's {@code @RequestBody byte[]}.
 *
 * <pre>{@code
 * // Spring Boot controller example
 * @PostMapping("/webhooks/nahupay")
 * public ResponseEntity<Map<String, Object>> webhook(
 *         @RequestBody byte[] rawBody,
 *         @RequestHeader("X-NahuPay-Signature") String signature) {
 *
 *     WebhookEvent event;
 *     try {
 *         event = NahuPay.webhooks().verify(rawBody, signature,
 *                 System.getenv("NAHUPAY_SECRET_KEY"));
 *     } catch (NahuPayWebhookException e) {
 *         return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
 *     }
 *
 *     if ("payment.success".equals(event.event)) {
 *         // fulfil the order …
 *     }
 *
 *     return ResponseEntity.ok(Map.of("received", true));
 * }
 * }</pre>
 */
public class NahuPayWebhooks {

    private static final ObjectMapper MAPPER = new ObjectMapper()
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    /**
     * Verify a webhook request from NahuPay and return the parsed event.
     *
     * @param rawBody   The <b>raw</b> request body as a {@code String}.
     *                  Do <em>not</em> pass an already-parsed object.
     * @param signature Value of the {@code X-NahuPay-Signature} header.
     * @param secret    Your secret API key ({@code sk_test_…} or {@code sk_live_…}).
     * @return The verified {@link WebhookEvent}.
     * @throws NahuPayWebhookException If the signature is missing, malformed, or does not match.
     */
    public WebhookEvent verify(String rawBody, String signature, String secret) {
        return verify(rawBody.getBytes(StandardCharsets.UTF_8), signature, secret);
    }

    /**
     * Verify a webhook request from NahuPay and return the parsed event.
     *
     * @param rawBody   The <b>raw</b> request body as a {@code byte[]}.
     * @param signature Value of the {@code X-NahuPay-Signature} header.
     * @param secret    Your secret API key ({@code sk_test_…} or {@code sk_live_…}).
     * @return The verified {@link WebhookEvent}.
     * @throws NahuPayWebhookException If the signature is missing, malformed, or does not match.
     */
    public WebhookEvent verify(byte[] rawBody, String signature, String secret) {
        if (signature == null || signature.isEmpty()) {
            throw new NahuPayWebhookException(
                    "Missing X-NahuPay-Signature header. "
                    + "Make sure you are passing the raw request body, not parsed JSON.");
        }

        final String prefix = "sha256=";
        if (!signature.startsWith(prefix)) {
            throw new NahuPayWebhookException(
                    "Unexpected signature format \"" + signature + "\". "
                    + "Expected \"sha256=<hex>\".");
        }

        String receivedHex = signature.substring(prefix.length());

        // Compute expected HMAC
        String expectedHex = hmacSha256Hex(rawBody, secret);

        // Constant-time comparison to prevent timing attacks
        byte[] received;
        byte[] expected;
        try {
            received = hexToBytes(receivedHex);
            expected = hexToBytes(expectedHex);
        } catch (IllegalArgumentException e) {
            throw new NahuPayWebhookException(
                    "Signature verification failed: received signature is malformed.");
        }

        if (!MessageDigest.isEqual(received, expected)) {
            throw new NahuPayWebhookException(
                    "Webhook signature verification failed. "
                    + "Ensure you are using the correct secret key and the raw request body.");
        }

        // Signature verified — safe to parse
        WebhookEvent event;
        try {
            event = MAPPER.readValue(rawBody, WebhookEvent.class);
        } catch (Exception e) {
            throw new NahuPayWebhookException(
                    "Webhook body is not valid JSON despite a valid signature. "
                    + "This should not happen — please contact NahuPay support.");
        }

        if (event.event == null || event.payment == null) {
            throw new NahuPayWebhookException(
                    "Webhook payload is missing required fields (`event` or `data`).");
        }

        return event;
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private static String hmacSha256Hex(byte[] data, String secret) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] hash = mac.doFinal(data);
            return bytesToHex(hash);
        } catch (Exception e) {
            throw new RuntimeException("Failed to compute HMAC-SHA256", e);
        }
    }

    /** Convert a hex string to a byte array (Java 11-compatible). */
    private static byte[] hexToBytes(String hex) {
        if (hex.length() % 2 != 0) {
            throw new IllegalArgumentException("Hex string has odd length: " + hex);
        }
        byte[] result = new byte[hex.length() / 2];
        for (int i = 0; i < result.length; i++) {
            int hi = Character.digit(hex.charAt(i * 2), 16);
            int lo = Character.digit(hex.charAt(i * 2 + 1), 16);
            if (hi == -1 || lo == -1) {
                throw new IllegalArgumentException("Invalid hex character in: " + hex);
            }
            result[i] = (byte) ((hi << 4) | lo);
        }
        return result;
    }

    /** Convert a byte array to a lowercase hex string (Java 11-compatible). */
    private static String bytesToHex(byte[] bytes) {
        StringBuilder sb = new StringBuilder(bytes.length * 2);
        for (byte b : bytes) {
            sb.append(String.format("%02x", b & 0xFF));
        }
        return sb.toString();
    }
}
