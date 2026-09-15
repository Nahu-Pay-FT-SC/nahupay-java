package com.nahupay.sdk;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.nahupay.sdk.exception.NahuPayApiException;

import java.io.IOException;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Thin {@link java.net.http.HttpClient} wrapper used internally by all resource classes.
 *
 * <p>Handles:
 * <ul>
 *   <li>Authorization header (Bearer &lt;apiKey&gt;)</li>
 *   <li>JSON serialisation / deserialisation via Jackson</li>
 *   <li>Standard ApiEnvelope unwrapping ({@code { success, data, message }})</li>
 *   <li>Request timeout</li>
 *   <li>{@link NahuPayApiException} on non-2xx or {@code success=false} responses</li>
 * </ul>
 */
class HttpClient {

    private static final String SDK_VERSION = "0.1.0";

    static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    private final java.net.http.HttpClient javaHttpClient;
    private final String baseUrl;
    private final String apiKey;

    HttpClient(NahuPayConfig config) {
        this.apiKey   = config.getApiKey();
        this.baseUrl  = config.getBaseUrl();
        this.javaHttpClient = java.net.http.HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();
    }

    // ─── Public helpers ───────────────────────────────────────────────────────

    <T> T get(String path, Map<String, String> query, TypeReference<ApiEnvelope<T>> typeRef) {
        String url = buildUrl(path, query);
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + apiKey)
                .header("Accept", "application/json")
                .header("User-Agent", "nahupay-java/" + SDK_VERSION)
                .GET()
                .build();
        return send(request, typeRef);
    }

    <T> T post(String path, Object body, TypeReference<ApiEnvelope<T>> typeRef) {
        String bodyJson;
        try {
            bodyJson = body != null ? MAPPER.writeValueAsString(body) : "{}";
        } catch (IOException e) {
            throw new IllegalArgumentException("Failed to serialize request body", e);
        }

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(baseUrl + path))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .header("User-Agent", "nahupay-java/" + SDK_VERSION)
                .POST(HttpRequest.BodyPublishers.ofString(bodyJson, StandardCharsets.UTF_8))
                .build();
        return send(request, typeRef);
    }

    // ─── Internal ─────────────────────────────────────────────────────────────

    private String buildUrl(String path, Map<String, String> query) {
        String base = baseUrl + path;
        if (query == null || query.isEmpty()) return base;

        String qs = query.entrySet().stream()
                .filter(e -> e.getValue() != null && !e.getValue().isEmpty())
                .map(e -> encode(e.getKey()) + "=" + encode(e.getValue()))
                .collect(Collectors.joining("&"));

        return qs.isEmpty() ? base : base + "?" + qs;
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private <T> T send(HttpRequest request, TypeReference<ApiEnvelope<T>> typeRef) {
        HttpResponse<String> response;
        try {
            response = javaHttpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (java.net.http.HttpTimeoutException e) {
            throw new NahuPayApiException("Request timed out", 0, "REQUEST_TIMEOUT", null);
        } catch (IOException | InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new NahuPayApiException("Connection error: " + e.getMessage(), 0);
        }

        int statusCode = response.statusCode();
        String bodyStr = response.body();

        ApiEnvelope<T> envelope;
        try {
            envelope = MAPPER.readValue(bodyStr, typeRef);
        } catch (IOException e) {
            throw new NahuPayApiException(
                    "Unexpected non-JSON response (HTTP " + statusCode + ")",
                    statusCode, "INVALID_RESPONSE", null);
        }

        if ((statusCode / 100) != 2 || Boolean.FALSE.equals(envelope.success)) {
            String msg = envelope.message != null
                    ? envelope.message
                    : "API error (HTTP " + statusCode + ")";
            throw new NahuPayApiException(msg, statusCode, envelope.errorCode, null);
        }

        return envelope.data;
    }

    // ─── ApiEnvelope wrapper ──────────────────────────────────────────────────

    static class ApiEnvelope<T> {
        public Boolean success;
        public String  message;
        public String  errorCode;
        public T       data;
    }
}
