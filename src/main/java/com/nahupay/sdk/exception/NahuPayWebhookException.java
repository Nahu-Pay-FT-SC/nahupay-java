package com.nahupay.sdk.exception;

/**
 * Thrown by {@link com.nahupay.sdk.webhook.NahuPayWebhooks#verify} when the
 * incoming request cannot be authenticated as a genuine NahuPay webhook.
 */
public class NahuPayWebhookException extends NahuPayException {

    public NahuPayWebhookException(String message) {
        super(message);
    }
}
