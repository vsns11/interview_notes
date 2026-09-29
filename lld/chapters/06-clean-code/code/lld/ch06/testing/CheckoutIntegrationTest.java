package lld.ch06.testing;

import static lld.ch06.testing.MiniTest.assertEquals;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

/** INTEGRATION test: real collaborating implementations wired together, no doubles for our own code. */
public class CheckoutIntegrationTest {

    @Test void full_checkout_flow_with_real_components() {
        var catalog = new Checkout.InMemoryCatalog().put("TEA", 12_000).put("CUP", 8_000);
        var coupons = new Checkout.InMemoryCoupons().save(new Checkout.Coupon("WELCOME", 25, LocalDate.of(2030, 1, 1)));
        var outbox = new Checkout.OutboxSender();
        var checkout = new Checkout(catalog, coupons, outbox, event -> {}, Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC));

        checkout.checkout(List.of(new Checkout.Line("TEA", 2), new Checkout.Line("CUP", 1)), "WELCOME", "meera@example.com");

        assertEquals(List.of(new Checkout.Receipt("meera@example.com", 24_000L)), outbox.outbox());
    }
}
