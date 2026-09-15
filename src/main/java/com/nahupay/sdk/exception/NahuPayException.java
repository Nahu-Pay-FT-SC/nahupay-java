package com.nahupay.sdk.exception;

/**
 * Base class for all exceptions thrown by the NahuPay Java SDK.
 */
public class NahuPayException extends RuntimeException {

    public NahuPayException(String message) {
        super(message);
    }

    public NahuPayException(String message, Throwable cause) {
        super(message, cause);
    }
}
