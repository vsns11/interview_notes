package lld.ch06.testing;

import static lld.ch06.testing.MiniTest.assertEquals;
import static lld.ch06.testing.MiniTest.assertThrows;
import static lld.ch06.testing.MiniTest.assertTrue;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** Clean UNIT tests: one concept each, Given/When/Then, every collaborator replaced by a test double. */
public class CheckoutTest {

    // ===== The five kinds of test double =====

    /** DUMMY: passed only to satisfy a parameter list; must never be used. */
    static final class DummyAuditLog implements Checkout.AuditLog {
        public void record(String event) { throw new AssertionError("dummy must not be called"); }
    }
    /** STUB: returns canned answers to the calls made during the test. */
    static final class StubCatalog implements Checkout.PriceCatalog {
        private final Map<String, Long> prices;
        StubCatalog(Map<String, Long> prices) { this.prices = prices; }
        public long pricePaise(String sku) { return prices.get(sku); }
    }
    /** FAKE: a working but simplified implementation (in-memory instead of a database). */
    static final class FakeCoupons implements Checkout.CouponRepository {
        private final List<Checkout.Coupon> rows = new ArrayList<>();
        FakeCoupons add(Checkout.Coupon c) { rows.add(c); return this; }
        public Optional<Checkout.Coupon> find(String code) { return rows.stream().filter(c -> c.code().equals(code)).findFirst(); }
    }
    /** SPY: a stub that also RECORDS how it was called, so the test can inspect it afterwards. */
    static final class SpySender implements Checkout.ReceiptSender {
        final List<Checkout.Receipt> sent = new ArrayList<>();
        public void send(Checkout.Receipt r) { sent.add(r); }
    }
    /** MOCK: pre-programmed with EXPECTATIONS and verifies them itself. */
    static final class MockAuditLog implements Checkout.AuditLog {
        private final List<String> expected = new ArrayList<>();
        private final List<String> actual = new ArrayList<>();
        MockAuditLog expect(String event) { expected.add(event); return this; }
        public void record(String event) { actual.add(event); }
        void verify() { assertEquals(expected, actual); }
    }

    // ===== Fixture helpers (a tiny domain-specific testing language) =====
    private static final LocalDate TODAY = LocalDate.of(2026, 9, 28);
    private static final Clock FIXED_CLOCK = Clock.fixed(Instant.parse("2026-09-28T10:00:00Z"), ZoneOffset.UTC); // Repeatable

    private final StubCatalog catalog = new StubCatalog(Map.of("TEA", 12_000L, "TV", 5_000_000L));
    private final SpySender sender = new SpySender();

    private Checkout checkoutWith(Checkout.CouponRepository coupons, Checkout.AuditLog audit) {
        return new Checkout(catalog, coupons, sender, audit, FIXED_CLOCK);
    }
    private static List<Checkout.Line> cart(Object... skuQtyPairs) {
        List<Checkout.Line> lines = new ArrayList<>();
        for (int i = 0; i < skuQtyPairs.length; i += 2) lines.add(new Checkout.Line((String) skuQtyPairs[i], (Integer) skuQtyPairs[i + 1]));
        return lines;
    }

    // ===== Tests =====
    @Test void quote_sums_price_times_quantity() {
        Checkout checkout = checkoutWith(new FakeCoupons(), new DummyAuditLog());   // Given
        long total = checkout.quote(cart("TEA", 3), null);                         // When
        assertEquals(36_000L, total);                                               // Then
    }

    @Test void valid_coupon_takes_percentage_off() {
        var coupons = new FakeCoupons().add(new Checkout.Coupon("DIWALI10", 10, TODAY));
        long total = checkoutWith(coupons, new DummyAuditLog()).quote(cart("TEA", 10), "DIWALI10");
        assertEquals(108_000L, total);
    }

    @Test void expired_coupon_is_ignored() {
        var coupons = new FakeCoupons().add(new Checkout.Coupon("OLD", 50, TODAY.minusDays(1)));
        long total = checkoutWith(coupons, new DummyAuditLog()).quote(cart("TEA", 1), "OLD");
        assertEquals(12_000L, total);
    }

    @Test void checkout_sends_exactly_one_receipt() {
        checkoutWith(new FakeCoupons(), new DummyAuditLog()).checkout(cart("TEA", 1), null, "asha@example.com");
        assertEquals(List.of(new Checkout.Receipt("asha@example.com", 12_000L)), sender.sent);   // inspect the spy
    }

    @Test void high_value_order_is_audited() {
        var audit = new MockAuditLog().expect("high-value order for ravi@example.com");
        checkoutWith(new FakeCoupons(), audit).checkout(cart("TV", 1), null, "ravi@example.com");
        audit.verify();                                                             // the mock checks itself
    }

    @Test void empty_cart_is_rejected() {
        var checkout = checkoutWith(new FakeCoupons(), new DummyAuditLog());
        var e = assertThrows(IllegalArgumentException.class, () -> checkout.checkout(List.of(), null, "x@example.com"));
        assertTrue(e.getMessage().contains("empty"), "message should explain the problem");
        assertTrue(sender.sent.isEmpty(), "nothing must be sent");
    }
}
