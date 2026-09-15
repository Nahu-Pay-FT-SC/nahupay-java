package com.nahupay.sdk;

import com.nahupay.sdk.resource.PaymentsResource;
import com.nahupay.sdk.resource.RefundsResource;
import com.nahupay.sdk.webhook.NahuPayWebhooks;

/**
 * Main entry point for the NahuPay Java SDK.
 *
 * <p><b>Quick start:</b>
 * <pre>{@code
 * NahuPay nahupay = new NahuPay(
 *     NahuPayConfig.builder()
 *         .apiKey(System.getenv("NAHUPAY_SECRET_KEY"))
 *         .build()
 * );
 *
 * Payment payment = nahupay.payments().create(
 *     CreatePaymentParams.builder()
 *         .amount(500)
 *         .customerEmail("abebe@example.com")
 *         .customerName("Abebe Bikila")
 *         .description("Order #1042")
 *         .returnUrl("https://myshop.com/ty?ref={PAYMENT_REFERENCE}")
 *         .webhookUrl("https://myshop.com/webhooks/nahupay")
 *         .metadata(Map.of("orderId", "1042"))
 *         .build()
 * );
 *
 * // Redirect the customer:
 * response.sendRedirect(payment.checkoutUrl);
 * }</pre>
 */
public class NahuPay {

    private static final NahuPayWebhooks WEBHOOKS_INSTANCE = new NahuPayWebhooks();

    private final PaymentsResource payments;
    private final RefundsResource  refunds;

    /**
     * Construct a new client.
     *
     * @param config SDK configuration (API key, optional base URL and timeout).
     * @throws IllegalArgumentException if the API key is invalid.
     */
    public NahuPay(NahuPayConfig config) {
        validateConfig(config);
        HttpClient http = new HttpClient(config);
        this.payments = new PaymentsResource(http);
        this.refunds  = new RefundsResource(http);
    }

    /**
     * Convenience constructor for the common case of a single API key.
     *
     * <pre>{@code
     * NahuPay nahupay = new NahuPay(System.getenv("NAHUPAY_SECRET_KEY"));
     * }</pre>
     */
    public NahuPay(String apiKey) {
        this(NahuPayConfig.of(apiKey));
    }

    // ─── Resources ────────────────────────────────────────────────────────────

    /**
     * Create, retrieve, list, cancel, refund, and simulate payments.
     */
    public PaymentsResource payments() {
        return payments;
    }

    /**
     * Create and list refunds (top-level aliases for convenience).
     */
    public RefundsResource refunds() {
        return refunds;
    }

    /**
     * Webhook signature verification utilities.
     *
     * <p>Available as a <b>static</b> method so you do not need an instance:
     * <pre>{@code
     * WebhookEvent event = NahuPay.webhooks().verify(rawBody, signature, secret);
     * }</pre>
     */
    public static NahuPayWebhooks webhooks() {
        return WEBHOOKS_INSTANCE;
    }

    // ─── Config validation ────────────────────────────────────────────────────

    private static void validateConfig(NahuPayConfig config) {
        String key = config.getApiKey();
        if (key == null || key.isEmpty()) {
            throw new IllegalArgumentException(
                    "NahuPay: apiKey is required. "
                    + "Pass your secret key: NahuPayConfig.builder().apiKey(\"sk_test_...\").build()");
        }
        if (!key.startsWith("sk_test_") && !key.startsWith("sk_live_")) {
            String preview = key.length() >= 10 ? key.substring(0, 10) : key;
            throw new IllegalArgumentException(
                    "NahuPay: Invalid API key format \"" + preview + "…\". "
                    + "Secret keys start with sk_test_ (sandbox) or sk_live_ (production).");
        }
        if (config.getTimeoutSeconds() <= 0) {
            throw new IllegalArgumentException(
                    "NahuPay: timeoutSeconds must be positive.");
        }
    }
}
