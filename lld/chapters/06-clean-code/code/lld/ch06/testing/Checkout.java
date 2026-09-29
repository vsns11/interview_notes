package lld.ch06.testing;

import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** The production code under test: a small checkout with four collaborators behind interfaces. */
public final class Checkout {

    public record Line(String sku, int qty) {}
    public record Coupon(String code, int percentOff, LocalDate expiresOn) {}
    public record Receipt(String email, long totalPaise) {}

    public interface PriceCatalog { long pricePaise(String sku); }
    public interface CouponRepository { Optional<Coupon> find(String code); }
    public interface ReceiptSender { void send(Receipt receipt); }
    public interface AuditLog { void record(String event); }

    static final long HIGH_VALUE_PAISE = 1_000_000;   // Rs 10,000

    private final PriceCatalog catalog;
    private final CouponRepository coupons;
    private final ReceiptSender sender;
    private final AuditLog audit;
    private final Clock clock;

    public Checkout(PriceCatalog catalog, CouponRepository coupons, ReceiptSender sender, AuditLog audit, Clock clock) {
        this.catalog = catalog; this.coupons = coupons; this.sender = sender; this.audit = audit; this.clock = clock;
    }

    /**
     * Query: computes the total, changes nothing.
     * {@code couponCode} may be {@code null}, meaning "no coupon" -- a documented part of this API's contract
     * (the "don't pass null" rule allows null only where an API explicitly expects it).
     */
    public long quote(List<Line> lines, String couponCode) {
        long subtotal = lines.stream().mapToLong(l -> catalog.pricePaise(l.sku()) * l.qty()).sum();
        return subtotal - discount(subtotal, couponCode);
    }

    /** Command: charges, sends the receipt, audits big orders. {@code couponCode} may be {@code null} (no coupon), as in {@link #quote}. */
    public Receipt checkout(List<Line> lines, String couponCode, String email) {
        if (lines.isEmpty()) throw new IllegalArgumentException("cart is empty");
        Receipt receipt = new Receipt(email, quote(lines, couponCode));
        sender.send(receipt);
        if (receipt.totalPaise() >= HIGH_VALUE_PAISE) audit.record("high-value order for " + email);
        return receipt;
    }

    private long discount(long subtotal, String couponCode) {
        if (couponCode == null) return 0;
        return coupons.find(couponCode)
                .filter(c -> !LocalDate.now(clock).isAfter(c.expiresOn()))
                .map(c -> subtotal * c.percentOff() / 100)
                .orElse(0L);
    }

    // ---------- simple real implementations, used by the integration test ----------
    public static final class InMemoryCatalog implements PriceCatalog {
        private final Map<String, Long> prices = new ConcurrentHashMap<>();
        public InMemoryCatalog put(String sku, long paise) { prices.put(sku, paise); return this; }
        public long pricePaise(String sku) {
            Long p = prices.get(sku);
            if (p == null) throw new IllegalArgumentException("unknown sku " + sku);
            return p;
        }
    }
    public static final class InMemoryCoupons implements CouponRepository {
        private final Map<String, Coupon> byCode = new ConcurrentHashMap<>();
        public InMemoryCoupons save(Coupon c) { byCode.put(c.code(), c); return this; }
        public Optional<Coupon> find(String code) { return Optional.ofNullable(byCode.get(code)); }
    }
    public static final class OutboxSender implements ReceiptSender {
        private final List<Receipt> outbox = new CopyOnWriteArrayList<>();
        public void send(Receipt r) { outbox.add(r); }
        public List<Receipt> outbox() { return List.copyOf(outbox); }
    }
}
