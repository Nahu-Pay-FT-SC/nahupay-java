package com.nahupay.sdk.params;

import com.fasterxml.jackson.annotation.JsonInclude;

import java.math.BigDecimal;

/**
 * Parameters for issuing a refund.
 *
 * <pre>{@code
 * // Full refund
 * CreateRefundParams params = CreateRefundParams.builder().build();
 *
 * // Partial refund
 * CreateRefundParams params = CreateRefundParams.builder()
 *     .amount(new BigDecimal("100.00"))
 *     .reason("Customer request")
 *     .build();
 * }</pre>
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class CreateRefundParams {

    /**
     * Amount to refund. {@code null} = full refund of the remaining
     * refundable balance.
     */
    public final BigDecimal amount;

    /** Reason string (max 255 chars). */
    public final String reason;

    private CreateRefundParams(Builder b) {
        this.amount = b.amount;
        this.reason = b.reason;
    }

    /** Returns a full-refund instance with no reason. */
    public static CreateRefundParams fullRefund() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private BigDecimal amount;
        private String     reason;

        public Builder amount(BigDecimal amount) { this.amount = amount; return this; }
        public Builder amount(double amount)     { this.amount = BigDecimal.valueOf(amount); return this; }
        public Builder reason(String reason)     { this.reason = reason; return this; }

        public CreateRefundParams build() {
            return new CreateRefundParams(this);
        }
    }
}
