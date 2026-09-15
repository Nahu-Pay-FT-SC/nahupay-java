package com.nahupay.sdk.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nahupay.sdk.HttpClient;
import com.nahupay.sdk.model.PageResponse;
import com.nahupay.sdk.model.Payment;
import com.nahupay.sdk.model.Refund;
import com.nahupay.sdk.params.CreatePaymentParams;
import com.nahupay.sdk.params.CreateRefundParams;
import com.nahupay.sdk.params.ListPaymentsParams;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Access via {@link com.nahupay.sdk.NahuPay#payments()}.
 *
 * <pre>{@code
 * NahuPay nahupay = new NahuPay(NahuPayConfig.of(System.getenv("NAHUPAY_SECRET_KEY")));
 *
 * Payment payment = nahupay.payments().create(
 *     CreatePaymentParams.builder()
 *         .amount(500)
 *         .customerEmail("abebe@example.com")
 *         .returnUrl("https://myshop.com/ty?ref={PAYMENT_REFERENCE}")
 *         .webhookUrl("https://myshop.com/webhooks/nahupay")
 *         .metadata(Map.of("orderId", "1042"))
 *         .build()
 * );
 *
 * // Redirect the customer:
 * System.out.println(payment.checkoutUrl);
 * }</pre>
 */
public class PaymentsResource {

    private final HttpClient http;

    public PaymentsResource(HttpClient http) {
        this.http = http;
    }

    // ─── Core operations ──────────────────────────────────────────────────────

    /**
     * Create a new payment and get back a hosted {@code checkoutUrl}.
     *
     * @param params Payment creation parameters.
     * @return The created {@link Payment}.
     */
    public Payment create(CreatePaymentParams params) {
        return http.post("/payments", params,
                new TypeReference<HttpClient.ApiEnvelope<Payment>>() {});
    }

    /**
     * Retrieve a single payment by its reference (e.g. {@code "PAY-20260523-ABCD1234"}).
     *
     * @param reference The payment reference.
     * @return The {@link Payment}.
     */
    public Payment retrieve(String reference) {
        return http.get("/payments/" + reference, null,
                new TypeReference<HttpClient.ApiEnvelope<Payment>>() {});
    }

    /**
     * List payments with optional server-side filtering.
     *
     * @param params Filter and pagination options.
     * @return A {@link PageResponse} of {@link Payment}.
     */
    public PageResponse<Payment> list(ListPaymentsParams params) {
        Map<String, String> query = new HashMap<>();
        if (params.status != null) query.put("status", params.status);
        query.put("page", String.valueOf(params.page));
        query.put("size", String.valueOf(params.size));

        return http.get("/payments", query,
                new TypeReference<HttpClient.ApiEnvelope<PageResponse<Payment>>>() {});
    }

    /**
     * List payments with default parameters (all statuses, first page, size 20).
     */
    public PageResponse<Payment> list() {
        return list(ListPaymentsParams.defaults());
    }

    /**
     * Cancel a pending payment. Only {@code PENDING} payments can be cancelled.
     *
     * @param reference The payment reference.
     * @return The updated {@link Payment}.
     */
    public Payment cancel(String reference) {
        return http.post("/payments/" + reference + "/cancel", null,
                new TypeReference<HttpClient.ApiEnvelope<Payment>>() {});
    }

    // ─── Refunds ──────────────────────────────────────────────────────────────

    /**
     * Issue a refund against a {@code SUCCESS} or {@code PARTIALLY_REFUNDED} payment.
     *
     * @param reference The payment reference.
     * @param params    Refund parameters. Use {@link CreateRefundParams#fullRefund()} for a full refund.
     * @return The created {@link Refund}.
     */
    public Refund refund(String reference, CreateRefundParams params) {
        return http.post("/payments/" + reference + "/refund", params,
                new TypeReference<HttpClient.ApiEnvelope<Refund>>() {});
    }

    /**
     * Full refund convenience method.
     */
    public Refund refund(String reference) {
        return refund(reference, CreateRefundParams.fullRefund());
    }

    /**
     * List all refunds issued against a payment.
     *
     * @param reference The payment reference.
     * @return List of {@link Refund}.
     */
    public List<Refund> listRefunds(String reference) {
        return http.get("/payments/" + reference + "/refunds", null,
                new TypeReference<HttpClient.ApiEnvelope<List<Refund>>>() {});
    }

    // ─── Test-mode simulation ─────────────────────────────────────────────────

    /**
     * <b>Test mode only.</b> Instantly mark a payment as {@code SUCCESS}.
     * Triggers webhooks and creates transactions exactly as a real payment would.
     *
     * @param reference The payment reference.
     * @return The updated {@link Payment}.
     * @throws com.nahupay.sdk.exception.NahuPayApiException If called with a {@code sk_live_} key (HTTP 403).
     */
    public Payment simulateSuccess(String reference) {
        return http.post("/payments/" + reference + "/simulate/success", null,
                new TypeReference<HttpClient.ApiEnvelope<Payment>>() {});
    }

    /**
     * <b>Test mode only.</b> Instantly mark a payment as {@code FAILED}.
     * Triggers the {@code payment.failed} webhook.
     *
     * @param reference The payment reference.
     * @return The updated {@link Payment}.
     * @throws com.nahupay.sdk.exception.NahuPayApiException If called with a {@code sk_live_} key (HTTP 403).
     */
    public Payment simulateFailure(String reference) {
        return http.post("/payments/" + reference + "/simulate/failure", null,
                new TypeReference<HttpClient.ApiEnvelope<Payment>>() {});
    }
}
