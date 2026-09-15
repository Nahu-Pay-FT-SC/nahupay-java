package com.nahupay.sdk.params;

/**
 * Parameters for listing payments via {@link com.nahupay.sdk.resource.PaymentsResource#list}.
 */
public final class ListPaymentsParams {

    /** Filter by payment status, e.g. {@code "SUCCESS"}. {@code null} = no filter. */
    public final String status;

    /** 0-based page index. Default: {@code 0}. */
    public final int page;

    /** Page size (max 100). Default: {@code 20}. */
    public final int size;

    private ListPaymentsParams(Builder b) {
        this.status = b.status;
        this.page   = b.page;
        this.size   = b.size;
    }

    /** Returns a default instance: all statuses, first page, size 20. */
    public static ListPaymentsParams defaults() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static final class Builder {
        private String status = null;
        private int    page   = 0;
        private int    size   = 20;

        public Builder status(String status) { this.status = status; return this; }
        public Builder page(int page)        { this.page = page;     return this; }
        public Builder size(int size)        { this.size = size;     return this; }

        public ListPaymentsParams build() {
            return new ListPaymentsParams(this);
        }
    }
}
