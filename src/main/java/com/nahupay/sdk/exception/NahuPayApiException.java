package com.nahupay.sdk.exception;

import java.util.Map;

/**
 * Thrown when the NahuPay API returns a non-2xx HTTP status or a
 * {@code {"success": false}} envelope.
 */
public class NahuPayApiException extends NahuPayException {

    /** HTTP status code (e.g. 400, 401, 404). {@code 0} = network / timeout error. */
    private final int statusCode;

    /**
     * Machine-readable error code from the API (e.g. {@code "NOT_FOUND"},
     * {@code "INVALID_API_KEY"}, {@code "LIVE_MODE_NOT_ENABLED"}).
     * May be {@code null}.
     */
    private final String errorCode;

    /** Full parsed response body as a map, or {@code null} if unavailable. */
    private final Map<String, Object> raw;

    public NahuPayApiException(String message, int statusCode, String errorCode,
                               Map<String, Object> raw) {
        super(message);
        this.statusCode = statusCode;
        this.errorCode  = errorCode;
        this.raw        = raw;
    }

    public NahuPayApiException(String message, int statusCode) {
        this(message, statusCode, null, null);
    }

    public int getStatusCode()              { return statusCode; }
    public String getErrorCode()            { return errorCode;  }
    public Map<String, Object> getRaw()     { return raw;        }

    @Override
    public String toString() {
        return "NahuPayApiException{statusCode=" + statusCode
                + ", errorCode='" + errorCode + "', message='" + getMessage() + "'}";
    }
}
