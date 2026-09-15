import com.nahupay.sdk.NahuPay;
import com.nahupay.sdk.NahuPayConfig;
import com.nahupay.sdk.exception.NahuPayApiException;
import com.nahupay.sdk.model.PageResponse;
import com.nahupay.sdk.model.Payment;
import com.nahupay.sdk.model.Refund;
import com.nahupay.sdk.params.CreatePaymentParams;
import com.nahupay.sdk.params.CreateRefundParams;
import com.nahupay.sdk.params.ListPaymentsParams;

import java.util.List;
import java.util.Map;

/**
 * End-to-end example: create, retrieve, simulate, and refund a payment.
 *
 * Run after adding the SDK to the classpath:
 *   mvn compile exec:java -Dexec.mainClass=CreatePaymentExample \
 *       -Dexec.args="" \
 *       -DNAHUPAY_SECRET_KEY=sk_test_...
 */
public class CreatePaymentExample {

    public static void main(String[] args) {
        String secretKey = System.getenv().getOrDefault("NAHUPAY_SECRET_KEY", "sk_test_replace_me");
        String apiUrl    = System.getenv().getOrDefault("NAHUPAY_API_URL", "http://localhost:8080/api/v1");

        NahuPay nahupay = new NahuPay(
                NahuPayConfig.builder()
                        .apiKey(secretKey)
                        .baseUrl(apiUrl)
                        .timeoutSeconds(30)
                        .build()
        );

        // ── 1. Create a payment ─────────────────────────────────────────────
        System.out.println("Creating payment …");
        Payment payment;
        try {
            payment = nahupay.payments().create(
                    CreatePaymentParams.builder()
                            .amount(500)
                            .customerEmail("abebe@example.com")
                            .customerName("Abebe Bikila")
                            .description("Order #1042 — 2 items")
                            .returnUrl("http://localhost:3002/thank-you?ref={PAYMENT_REFERENCE}")
                            .webhookUrl("http://localhost:3002/webhooks/nahupay")
                            .metadata(Map.of("orderId", "1042", "userId", "u_abc"))
                            .build()
            );
        } catch (NahuPayApiException e) {
            System.err.printf("Error: %s  (status=%d, code=%s)%n",
                    e.getMessage(), e.getStatusCode(), e.getErrorCode());
            return;
        }

        System.out.printf("  Reference  : %s%n", payment.reference);
        System.out.printf("  Status     : %s%n", payment.status);
        System.out.printf("  Checkout   : %s%n", payment.checkoutUrl);
        System.out.printf("  Expires at : %s%n%n", payment.expiresAt);

        // ── 2. Retrieve the payment ─────────────────────────────────────────
        System.out.println("Retrieving payment …");
        Payment fetched = nahupay.payments().retrieve(payment.reference);
        System.out.printf("  Status: %s%n%n", fetched.status);

        // ── 3. Simulate success (test mode only) ────────────────────────────
        if (secretKey.startsWith("sk_test_")) {
            System.out.println("Simulating success …");
            Payment paid = nahupay.payments().simulateSuccess(payment.reference);
            System.out.printf("  Status  : %s%n", paid.status);
            System.out.printf("  Paid at : %s%n%n", paid.paidAt);

            // ── 4. Partial refund ───────────────────────────────────────────
            System.out.println("Issuing partial refund (ETB 100) …");
            Refund refund = nahupay.payments().refund(
                    payment.reference,
                    CreateRefundParams.builder().amount(100).reason("Customer request").build()
            );
            System.out.printf("  Refund ref : %s%n", refund.refundReference);
            System.out.printf("  Status     : %s%n%n", refund.status);

            // ── 5. List refunds ─────────────────────────────────────────────
            System.out.println("Listing refunds …");
            List<Refund> refunds = nahupay.payments().listRefunds(payment.reference);
            for (Refund r : refunds) {
                System.out.printf("  %s  ETB %s  %s%n",
                        r.refundReference, r.amount, r.status);
            }
            System.out.println();
        }

        // ── 6. List payments ─────────────────────────────────────────────────
        System.out.println("Listing recent payments …");
        PageResponse<Payment> page = nahupay.payments().list(
                ListPaymentsParams.builder().size(5).build()
        );
        System.out.printf("  Total: %d payments (%d pages)%n",
                page.totalElements, page.totalPages);
        for (Payment p : page.content) {
            System.out.printf("  %s  ETB %s  %s%n", p.reference, p.amount, p.status);
        }
    }
}
