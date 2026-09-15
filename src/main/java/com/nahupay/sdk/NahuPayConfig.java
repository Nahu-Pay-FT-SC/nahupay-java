package com.nahupay.sdk;

/**
 * Configuration for the {@link NahuPay} client.
 *
 * <p>Build with the fluent builder:
 * <pre>{@code
 * NahuPayConfig config = NahuPayConfig.builder()
 *     .apiKey(System.getenv("NAHUPAY_SECRET_KEY"))
 *     .baseUrl("http://localhost:8080/api/v1")  // omit in production
 *     .timeoutSeconds(30)
 *     .build();
 *
 * NahuPay nahupay = new NahuPay(config);
 * }</pre>
 */
public final class NahuPayConfig {

    static final String DEFAULT_BASE_URL   = "https://api.nahupay.com/api/v1";
    static final int    DEFAULT_TIMEOUT_S  = 30;

    private final String apiKey;
    private final String baseUrl;
    private final int    timeoutSeconds;

    private NahuPayConfig(Builder builder) {
        this.apiKey         = builder.apiKey;
        this.baseUrl        = builder.baseUrl != null
                ? builder.baseUrl.replaceAll("/$", "")
                : DEFAULT_BASE_URL;
        this.timeoutSeconds = builder.timeoutSeconds > 0
                ? builder.timeoutSeconds
                : DEFAULT_TIMEOUT_S;
    }

    public String getApiKey()       { return apiKey;         }
    public String getBaseUrl()      { return baseUrl;        }
    public int    getTimeoutSeconds() { return timeoutSeconds; }

    /** Convenience constructor for the common single-key case. */
    public static NahuPayConfig of(String apiKey) {
        return builder().apiKey(apiKey).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String apiKey;
        private String baseUrl;
        private int    timeoutSeconds = DEFAULT_TIMEOUT_S;

        /**
         * Your secret API key. Use {@code sk_test_…} for sandbox and
         * {@code sk_live_…} for production. Required.
         */
        public Builder apiKey(String apiKey) {
            this.apiKey = apiKey;
            return this;
        }

        /**
         * Override the API base URL.
         * Default: {@value NahuPayConfig#DEFAULT_BASE_URL}.
         * Set to {@code http://localhost:8080/api/v1} for local development.
         */
        public Builder baseUrl(String baseUrl) {
            this.baseUrl = baseUrl;
            return this;
        }

        /**
         * Per-request timeout in seconds. Default: {@value NahuPayConfig#DEFAULT_TIMEOUT_S}.
         */
        public Builder timeoutSeconds(int timeoutSeconds) {
            this.timeoutSeconds = timeoutSeconds;
            return this;
        }

        public NahuPayConfig build() {
            return new NahuPayConfig(this);
        }
    }
}
