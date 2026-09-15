package com.nahupay.sdk.params;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Parameters for creating a new payment via {@link com.nahupay.sdk.resource.PaymentsResource#create}.
 *
 * <p>Build with the fluent builder:
 * <pre>{@code
 * CreatePaymentParams params = CreatePaymentParams.builder()
 *     .amount(new BigDecimal("500.00"))
 *     .customerEmail("abebe@example.com")
 *     .customerName("Abebe Bikila")
 *     .description("Order #1042")
 *     .returnUrl("https://myshop.com/ty?ref={PAYMENT_REFERENCE}")
 *     .webhookUrl("https://myshop.com/webhooks/nahupay")
 *     .metadata(Map.of("orderId", "1042"))
 *     .build();
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreatePaymentParams {

    /** Amount in ETB. Minimum 1.00. Required. */
    public final BigDecimal amount;

    /** Customer's email address. Required. */
    public final String customerEmail;

    public final String customerName;
    public final String customerPhone;

    /** ISO currency code. Default "ETB". */
    public final String currency;

    /** Short description shown on the checkout page (max 500 chars). */
    public final String description;

    /**
     * Internal provider-callback URL. Usually omit this — NahuPay generates
     * the correct URL automatically (max 500 chars).
     */
    public final String callbackUrl;

    /**
     * Where to redirect the customer after payment (max 500 chars).
     * Use {@code {PAYMENT_REFERENCE}} as a placeholder,
     * e.g. {@code "https://shop.com/ty?ref={PAYMENT_REFERENCE}"}.
     */
    public final String returnUrl;

    /** Your server-side webhook endpoint (max 500 chars). */
    public final String webhookUrl;

    /**
     * Arbitrary metadata serialised to a JSON string.
     * Pass a {@link Map} — the SDK serialises it to JSON.
     * Retrieve later via {@link com.nahupay.sdk.model.Payment#metadata}.
     */
    public final String metadata;

    /**
     * Idempotency key. If a payment with this key was already created for
     * your account, the existing payment is returned (max 255 chars).
     */
    public final String idempotencyKey;

    private CreatePaymentParams(Builder b) {
        this.amount         = b.amount;
        this.customerEmail  = b.customerEmail;
        this.customerName   = b.customerName;
        this.customerPhone  = b.customerPhone;
        this.currency       = b.currency != null ? b.currency : "ETB";
        this.description    = b.description;
        this.callbackUrl    = b.callbackUrl;
        this.returnUrl      = b.returnUrl;
        this.webhookUrl     = b.webhookUrl;
        this.idempotencyKey = b.idempotencyKey;

        if (b.metadataMap != null) {
            try {
                this.metadata = com.nahupay.sdk.HttpClient.MAPPER.writeValueAsString(b.metadataMap);
            } catch (Exception e) {
                throw new IllegalArgumentException("Failed to serialize metadata", e);
            }
        } else {
            this.metadata = b.metadataString;
        }
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BigDecimal          amount;
        private String              customerEmail;
        private String              customerName;
        private String              customerPhone;
        private String              currency;
        private String              description;
        private String              callbackUrl;
        private String              returnUrl;
        private String              webhookUrl;
        private Map<String, Object> metadataMap;
        private String              metadataString;
        private String              idempotencyKey;

        public Builder amount(BigDecimal amount)           { this.amount = amount;                 return this; }
        public Builder amount(double amount)               { this.amount = BigDecimal.valueOf(amount); return this; }
        public Builder customerEmail(String email)         { this.customerEmail = email;            return this; }
        public Builder customerName(String name)           { this.customerName = name;              return this; }
        public Builder customerPhone(String phone)         { this.customerPhone = phone;            return this; }
        public Builder currency(String currency)           { this.currency = currency;              return this; }
        public Builder description(String description)     { this.description = description;        return this; }
        public Builder callbackUrl(String callbackUrl)     { this.callbackUrl = callbackUrl;        return this; }
        public Builder returnUrl(String returnUrl)         { this.returnUrl = returnUrl;            return this; }
        public Builder webhookUrl(String webhookUrl)       { this.webhookUrl = webhookUrl;          return this; }
        public Builder metadata(Map<String, Object> meta)  { this.metadataMap = meta;              return this; }
        public Builder metadata(String metadataJson)       { this.metadataString = metadataJson;   return this; }
        public Builder idempotencyKey(String key)          { this.idempotencyKey = key;            return this; }

        public CreatePaymentParams build() {
            if (amount == null)        throw new IllegalStateException("amount is required");
            if (customerEmail == null) throw new IllegalStateException("customerEmail is required");
            return new CreatePaymentParams(this);
        }
    }
}
