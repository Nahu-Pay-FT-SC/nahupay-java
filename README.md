# nahupay-java

Official Java SDK for the **NahuPay** payment platform.

Uses Java 11's built-in `java.net.http.HttpClient` — zero additional HTTP dependencies.
Jackson is used for JSON parsing.

---

## Installation

### Maven

```xml
<dependency>
    <groupId>com.nahupay</groupId>
    <artifactId>nahupay-java</artifactId>
    <version>0.1.0</version>
</dependency>
```

### Gradle

```groovy
implementation 'com.nahupay:nahupay-java:0.1.0'
```

---

## Quick start

```java
import com.nahupay.sdk.NahuPay;
import com.nahupay.sdk.NahuPayConfig;
import com.nahupay.sdk.model.Payment;
import com.nahupay.sdk.params.CreatePaymentParams;

NahuPay nahupay = new NahuPay(
    NahuPayConfig.builder()
        .apiKey(System.getenv("NAHUPAY_SECRET_KEY"))
        .build()
);
// Or with a local dev API:
NahuPay nahupay = new NahuPay(
    NahuPayConfig.builder()
        .apiKey(System.getenv("NAHUPAY_SECRET_KEY"))
        .baseUrl("http://localhost:8080/api/v1")
        .build()
);
```

---

## Payments

### Create a payment

```java
Payment payment = nahupay.payments().create(
    CreatePaymentParams.builder()
        .amount(500)                                  // ETB, minimum 1.00
        .customerEmail("abebe@example.com")
        .customerName("Abebe Bikila")
        .description("Order #1042 — 2 items")
        .returnUrl("https://myshop.com/ty?ref={PAYMENT_REFERENCE}")
        .webhookUrl("https://myshop.com/webhooks/nahupay")
        .metadata(Map.of("orderId", "1042", "userId", "u_abc"))
        .build()
);

// Redirect the customer to the hosted checkout page
response.sendRedirect(payment.checkoutUrl);
```

### Retrieve, list, cancel

```java
Payment payment = nahupay.payments().retrieve("PAY-20260523-ABCD1234");
System.out.println(payment.status); // "SUCCESS"

PageResponse<Payment> page = nahupay.payments().list(
    ListPaymentsParams.builder().status("SUCCESS").size(50).build()
);
System.out.println(page.totalElements);
page.content.forEach(p -> System.out.println(p.reference + " " + p.status));

nahupay.payments().cancel("PAY-xxx");
```

---

## Refunds

```java
// Full refund
Refund refund = nahupay.payments().refund("PAY-xxx");

// Partial refund
Refund refund = nahupay.payments().refund(
    "PAY-xxx",
    CreateRefundParams.builder().amount(100).reason("Customer request").build()
);

// List refunds
List<Refund> refunds = nahupay.payments().listRefunds("PAY-xxx");

// Top-level aliases
Refund refund   = nahupay.refunds().create("PAY-xxx", CreateRefundParams.builder().amount(100).build());
List<Refund> rs = nahupay.refunds().list("PAY-xxx");
```

---

## Webhooks

### Spring Boot controller example

```java
import com.nahupay.sdk.NahuPay;
import com.nahupay.sdk.exception.NahuPayWebhookException;
import com.nahupay.sdk.model.WebhookEvent;

@RestController
public class WebhookController {

    @PostMapping("/webhooks/nahupay")
    public ResponseEntity<Map<String, Object>> webhook(
            @RequestBody byte[] rawBody,
            @RequestHeader("X-NahuPay-Signature") String signature) {

        WebhookEvent event;
        try {
            event = NahuPay.webhooks().verify(
                rawBody,
                signature,
                System.getenv("NAHUPAY_SECRET_KEY")
            );
        } catch (NahuPayWebhookException e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }

        if ("payment.success".equals(event.event)) {
            // fulfil the order …
            log.info("Payment {} succeeded — ETB {}", event.payment.reference, event.payment.amount);
        }

        if ("payment.failed".equals(event.event)) {
            log.warn("Payment {} failed: {}", event.payment.reference, event.payment.failureReason);
        }

        return ResponseEntity.ok(Map.of("received", true));
    }
}
```

### ⚠️ Important

- Use `@RequestBody byte[]` to receive the **raw bytes** — not `@RequestBody String`
  or `@RequestBody SomeDto` — or the signature will not match.
- The `X-NahuPay-Signature` header contains `sha256=<hex-digest>`.

---

## Test mode & simulation

```java
// Instantly mark as SUCCESS (fires webhook)
Payment paid = nahupay.payments().simulateSuccess("PAY-xxx");

// Instantly mark as FAILED
Payment failed = nahupay.payments().simulateFailure("PAY-xxx");
```

Both throw `NahuPayApiException` (HTTP 403) when called with a `sk_live_` key.

---

## Error handling

```java
import com.nahupay.sdk.exception.NahuPayApiException;
import com.nahupay.sdk.exception.NahuPayWebhookException;

try {
    nahupay.payments().retrieve("PAY-does-not-exist");
} catch (NahuPayApiException e) {
    System.out.println(e.getStatusCode());  // 404
    System.out.println(e.getErrorCode());   // "NOT_FOUND"
    System.out.println(e.getMessage());     // "Payment not found"
}
```

| Exception | When thrown |
|-----------|-------------|
| `NahuPayApiException` | Non-2xx HTTP or `success=false` from the API |
| `NahuPayWebhookException` | Invalid / missing webhook signature |
| `NahuPayException` | Base class — catch to handle all SDK errors |

---

## SDK architecture

| Class | Role |
|-------|------|
| `NahuPay` | Main entry point — validates key, exposes `payments()`, `refunds()`, `static webhooks()` |
| `NahuPayConfig` / `NahuPayConfig.Builder` | SDK configuration |
| `HttpClient` | Java 11 HTTP wrapper — auth headers, JSON (Jackson), envelope unwrap, error handling |
| `PaymentsResource` | All 8 payment methods |
| `RefundsResource` | create + list |
| `NahuPayWebhooks` | `verify()` — HMAC-SHA256 with constant-time comparison |
| `model/Payment` | Payment response model |
| `model/Refund` | Refund response model |
| `model/WebhookEvent` | Webhook event model |
| `model/PageResponse<T>` | Paginated result wrapper |
| `params/CreatePaymentParams` | Payment creation parameters (builder) |
| `params/ListPaymentsParams` | List filter parameters (builder) |
| `params/CreateRefundParams` | Refund parameters (builder) |
| `exception/NahuPayApiException` | API errors with `statusCode` + `errorCode` |
| `exception/NahuPayWebhookException` | Webhook verification failures |

---

## Requirements

- Java 11+
- Jackson Databind 2.x (declared as a Maven dependency)

The `java.net.http.HttpClient` is part of the JDK since Java 11 — no extra HTTP library needed.
