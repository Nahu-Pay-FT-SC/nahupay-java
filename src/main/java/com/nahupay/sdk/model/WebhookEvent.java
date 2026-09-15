package com.nahupay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

/**
 * A verified NahuPay webhook event.
 *
 * <p>Returned by {@link com.nahupay.sdk.webhook.NahuPayWebhooks#verify}.
 *
 * <p>Event types:
 * <ul>
 *   <li>{@code "payment.success"} — payment completed. Safe to fulfil the order.</li>
 *   <li>{@code "payment.failed"}  — payment failed. Notify the customer.</li>
 * </ul>
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class WebhookEvent {

    /** Event type, e.g. {@code "payment.success"} or {@code "payment.failed"}. */
    @JsonProperty("event")     public String event;

    /** ISO-8601 timestamp when the event was generated. */
    @JsonProperty("timestamp") public String timestamp;

    /** The payment this event is about. */
    @JsonProperty("data")      public Payment payment;

    @Override
    public String toString() {
        return "WebhookEvent{event='" + event
                + "', payment=" + (payment != null ? payment.reference : "null") + "}";
    }
}
