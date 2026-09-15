package com.nahupay.sdk;

import com.nahupay.sdk.exception.NahuPayWebhookException;
import com.nahupay.sdk.model.WebhookEvent;
import org.junit.jupiter.api.Test;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class WebhookVerificationTest {

    private static final String SECRET = "sk_test_abc123";

    private static final String SAMPLE_PAYLOAD = """
            {
              "event": "payment.success",
              "timestamp": "2026-05-23T11:05:00",
              "data": {
                "id": "550e8400-e29b-41d4-a716-446655440000",
                "reference": "PAY-20260523-ABCD1234",
                "amount": "500.00",
                "currency": "ETB",
                "status": "SUCCESS",
                "mode": "TEST",
                "customerEmail": "abebe@example.com",
                "customerName": "Abebe Bikila",
                "checkoutUrl": "https://checkout.nahupay.com/PAY-20260523-ABCD1234",
                "expiresAt": "2026-05-23T12:00:00",
                "createdAt": "2026-05-23T11:00:00"
              }
            }
            """;

    private static String sign(String body, String secret) throws Exception {
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
        byte[] hash = mac.doFinal(body.getBytes(StandardCharsets.UTF_8));
        StringBuilder sb = new StringBuilder();
        for (byte b : hash) sb.append(String.format("%02x", b & 0xFF));
        return "sha256=" + sb;
    }

    @Test
    void validSignature_returnsEvent() throws Exception {
        String sig = sign(SAMPLE_PAYLOAD, SECRET);
        WebhookEvent event = NahuPay.webhooks().verify(SAMPLE_PAYLOAD, sig, SECRET);

        assertNotNull(event);
        assertEquals("payment.success", event.event);
        assertNotNull(event.payment);
        assertEquals("PAY-20260523-ABCD1234", event.payment.reference);
    }

    @Test
    void validBytesBody_returnsEvent() throws Exception {
        byte[] body = SAMPLE_PAYLOAD.getBytes(StandardCharsets.UTF_8);
        String sig = sign(SAMPLE_PAYLOAD, SECRET);
        WebhookEvent event = NahuPay.webhooks().verify(body, sig, SECRET);

        assertEquals("payment.success", event.event);
    }

    @Test
    void missingSignature_throws() {
        assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(SAMPLE_PAYLOAD, null, SECRET));
    }

    @Test
    void emptySignature_throws() {
        assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(SAMPLE_PAYLOAD, "", SECRET));
    }

    @Test
    void wrongPrefix_throws() {
        NahuPayWebhookException ex = assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(SAMPLE_PAYLOAD, "md5=abc123", SECRET));
        assertTrue(ex.getMessage().contains("Unexpected signature format"));
    }

    @Test
    void wrongSecret_throws() throws Exception {
        String sig = sign(SAMPLE_PAYLOAD, "wrong_secret");
        assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(SAMPLE_PAYLOAD, sig, SECRET));
    }

    @Test
    void tamperedBody_throws() throws Exception {
        String sig = sign(SAMPLE_PAYLOAD, SECRET);
        String tampered = SAMPLE_PAYLOAD.replace("payment.success", "payment.failed");
        assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(tampered, sig, SECRET));
    }

    @Test
    void malformedHex_throws() {
        NahuPayWebhookException ex = assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(SAMPLE_PAYLOAD, "sha256=not-valid-hex!!", SECRET));
        assertTrue(ex.getMessage().contains("malformed"));
    }

    @Test
    void invalidJson_throws() throws Exception {
        String body = "not json at all";
        String sig = sign(body, SECRET);
        assertThrows(NahuPayWebhookException.class, () ->
                NahuPay.webhooks().verify(body, sig, SECRET));
    }
}
