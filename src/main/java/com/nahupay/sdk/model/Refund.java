package com.nahupay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.math.BigDecimal;

/**
 * A NahuPay refund object.
 *
 * <p>Returned by {@code PaymentsResource.refund()}, {@code RefundsResource.create()},
 * and list variants.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class Refund {

    @JsonProperty("id")                 public String id;
    @JsonProperty("refundReference")    public String refundReference;
    @JsonProperty("paymentReference")   public String paymentReference;
    @JsonProperty("amount")             public BigDecimal amount;
    @JsonProperty("reason")             public String reason;
    @JsonProperty("status")             public String status;
    @JsonProperty("mode")               public String mode;
    @JsonProperty("createdAt")          public String createdAt;

    @Override
    public String toString() {
        return "Refund{refundReference='" + refundReference
                + "', amount=" + amount + ", status='" + status + "'}";
    }
}
