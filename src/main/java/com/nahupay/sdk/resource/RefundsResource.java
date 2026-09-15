package com.nahupay.sdk.resource;

import com.fasterxml.jackson.core.type.TypeReference;
import com.nahupay.sdk.HttpClient;
import com.nahupay.sdk.model.Refund;
import com.nahupay.sdk.params.CreateRefundParams;

import java.util.List;

/**
 * Top-level refunds resource — mirrors {@link PaymentsResource#refund} but
 * provides a standalone namespace for code that works exclusively with refunds.
 *
 * <p>Access via {@link com.nahupay.sdk.NahuPay#refunds()}.
 *
 * <pre>{@code
 * Refund refund = nahupay.refunds().create(
 *     "PAY-xxx",
 *     CreateRefundParams.builder()
 *         .amount(250)
 *         .reason("Customer request — partial return")
 *         .build()
 * );
 * System.out.println(refund.refundReference); // "REF-20260523-..."
 * System.out.println(refund.status);          // "PENDING"
 * }</pre>
 */
public class RefundsResource {

    private final HttpClient http;

    public RefundsResource(HttpClient http) {
        this.http = http;
    }

    /**
     * Issue a refund against a payment.
     *
     * @param paymentReference The reference of the payment to refund.
     * @param params           Refund parameters. Use {@link CreateRefundParams#fullRefund()} for a full refund.
     * @return The created {@link Refund}.
     */
    public Refund create(String paymentReference, CreateRefundParams params) {
        return http.post("/payments/" + paymentReference + "/refund", params,
                new TypeReference<HttpClient.ApiEnvelope<Refund>>() {});
    }

    /**
     * Full refund convenience method.
     */
    public Refund create(String paymentReference) {
        return create(paymentReference, CreateRefundParams.fullRefund());
    }

    /**
     * List all refunds for a given payment.
     *
     * @param paymentReference The payment reference.
     * @return List of {@link Refund}.
     */
    public List<Refund> list(String paymentReference) {
        return http.get("/payments/" + paymentReference + "/refunds", null,
                new TypeReference<HttpClient.ApiEnvelope<List<Refund>>>() {});
    }
}
