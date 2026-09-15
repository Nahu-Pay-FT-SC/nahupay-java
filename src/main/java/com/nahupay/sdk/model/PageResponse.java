package com.nahupay.sdk.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

/**
 * A paginated result from the NahuPay API, mirroring Spring's {@code Page<T>}.
 *
 * @param <T> The type of items on this page.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PageResponse<T> {

    /** The items on this page. */
    @JsonProperty("content")          public List<T> content;

    /** Total number of matching items across all pages. */
    @JsonProperty("totalElements")    public long totalElements;

    /** Total number of pages. */
    @JsonProperty("totalPages")       public int totalPages;

    /** Current page index (0-based). */
    @JsonProperty("number")           public int number;

    /** Requested page size. */
    @JsonProperty("size")             public int size;

    @JsonProperty("numberOfElements") public int numberOfElements;

    /** {@code true} if this is the first page. */
    @JsonProperty("first")            public boolean first;

    /** {@code true} if this is the last page. */
    @JsonProperty("last")             public boolean last;

    /** {@code true} if {@link #content} is empty. */
    @JsonProperty("empty")            public boolean empty;

    @Override
    public String toString() {
        return "PageResponse{number=" + number + ", size=" + size
                + ", totalElements=" + totalElements
                + ", items=" + (content != null ? content.size() : 0) + "}";
    }
}
