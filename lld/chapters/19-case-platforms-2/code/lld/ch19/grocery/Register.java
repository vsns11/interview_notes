package lld.ch19.grocery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

/** Observer: purchasing, the store manager's dashboard, shrinkage reports. */
interface StockListener {
    void stockAlert(String sku, BigDecimal onHand, String reason);
}

/**
 * Store stock shared by every register. A till never refuses a sale because the SYSTEM says zero:
 * the item is physically in the customer's hand, so the count is what is wrong. Stock may go
 * negative, and that is reported as a discrepancy for a recount.
 */
final class Inventory {
    private final Map<String, BigDecimal> onHand = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> reorderPoint = new ConcurrentHashMap<>();
    private final List<StockListener> listeners = new CopyOnWriteArrayList<>();

    void set(String sku, String qty, String reorderAt) {
        onHand.put(sku, new BigDecimal(qty));
        reorderPoint.put(sku, new BigDecimal(reorderAt));
    }
    void addListener(StockListener l) { listeners.add(l); }
    BigDecimal onHand(String sku) { return onHand.getOrDefault(sku, BigDecimal.ZERO); }

    /** delta < 0 for a sale, > 0 for a return. merge() is atomic per key, so registers never lose updates. */
    void adjust(String sku, BigDecimal delta) {
        BigDecimal after = onHand.merge(sku, delta, BigDecimal::add);
        BigDecimal before = after.subtract(delta);
        BigDecimal point = reorderPoint.getOrDefault(sku, BigDecimal.ZERO);
        // edge-triggered: alert only when THIS change crosses the line, so each crossing alerts exactly once
        if (before.compareTo(point) > 0 && after.compareTo(point) <= 0)
            listeners.forEach(l -> l.stockAlert(sku, after, "LOW - reorder"));
        if (before.signum() >= 0 && after.signum() < 0)
            listeners.forEach(l -> l.stockAlert(sku, after, "NEGATIVE - recount the shelf"));
    }
}

/** Immutable proof of purchase. */
record Receipt(String id, String registerId, List<LineItem> lines, PriceBreakdown breakdown,
               List<Payment> payments, BigDecimal change) {

    String render() {
        StringBuilder sb = new StringBuilder();
        sb.append("  ---------------- RECEIPT ").append(id).append(" (").append(registerId).append(") ----------------\n");
        for (LineItem l : lines) {
            String qty = l.product().unit() == PricingUnit.PER_KG
                    ? l.quantity().setScale(3, RoundingMode.UNNECESSARY) + " kg @ " + Money.fmt(l.product().unitPrice()) + "/kg"
                    : l.quantity().toPlainString() + " x " + Money.fmt(l.product().unitPrice());
            sb.append("  %2d %-18s %-24s %12s%s%n".formatted(l.lineNo(), l.product().name(), qty, Money.fmt(l.gross()),
                    l.voided() ? "  VOID" : ""));
        }
        sb.append("  %-46s %12s%n".formatted("Gross", Money.fmt(breakdown.gross())));
        for (Discount d : breakdown.discounts())
            sb.append("  %-46s %12s%n".formatted("  " + d.description(), "-" + Money.fmt(d.amount())));
        sb.append("  %-46s %12s%n".formatted("TOTAL", Money.fmt(breakdown.total())));
        breakdown.taxByCategory().forEach((cat, tax) ->
                sb.append("  %-46s %12s%n".formatted("  incl. tax " + cat + " @" + cat.ratePercent + "%", Money.fmt(tax))));
        for (Payment p : payments) sb.append("  %-46s %12s%n".formatted("Paid " + describe(p.tender()), Money.fmt(p.tender().amount())));
        if (change.signum() > 0) sb.append("  %-46s %12s%n".formatted("Change", Money.fmt(change)));
        return sb.toString();
    }

    private static String describe(Tender t) {
        return switch (t) {
            case Cash c -> "cash";
            case Card c -> "card **" + c.last4();
            case Upi u -> "UPI " + u.vpa();
        };
    }
}

/** Receipts from every register, plus what has already been returned against each (shared, thread-safe). */
final class ReceiptStore {
    private final Map<String, Receipt> receipts = new ConcurrentHashMap<>();
    private final Map<String, BigDecimal> returned = new ConcurrentHashMap<>();   // key receiptId/sku
    private final AtomicLong nextId = new AtomicLong(1);

    String newId() { return "R%05d".formatted(nextId.getAndIncrement()); }
    void save(Receipt r) { receipts.put(r.id(), r); }
    Receipt get(String id) {
        Receipt r = receipts.get(id);
        if (r == null) throw new PosException("Unknown receipt " + id);
        return r;
    }

    /** Atomically claims qty units for return (two counters cannot refund the same unit twice); returns the total claimed so far. */
    BigDecimal claimReturn(Receipt r, String sku, BigDecimal qty, BigDecimal bought) {
        return returned.compute(r.id() + "/" + sku, (k, already) -> {
            BigDecimal next = (already == null ? BigDecimal.ZERO : already).add(qty);
            if (next.compareTo(bought) > 0)
                throw new PosException("Only " + bought.subtract(next.subtract(qty)).stripTrailingZeros().toPlainString()
                        + " of " + sku + " left to return on " + r.id());
            return next;
        });
    }
}

/**
 * Facade for one checkout lane. A register is driven by one cashier (one thread); the things it
 * shares with other registers — catalog, inventory, receipts — are the thread-safe ones.
 */
final class Register {
    private final String id;
    private final Catalog catalog;
    private final PricingEngine engine;
    private final Inventory inventory;
    private final ReceiptStore receipts;
    private final PaymentGateway gateway;
    private Sale sale;
    private int saleNo = 1;

    Register(String id, Catalog catalog, PricingEngine engine, Inventory inventory, ReceiptStore receipts, PaymentGateway gateway) {
        this.id = id; this.catalog = catalog; this.engine = engine;
        this.inventory = inventory; this.receipts = receipts; this.gateway = gateway;
    }

    void startSale() {
        if (sale != null && (sale.status() == SaleStatus.OPEN || sale.status() == SaleStatus.TENDERING))
            throw new PosException("Finish or cancel sale " + sale.id + " first");
        sale = new Sale(id + "-S" + saleNo++);
    }

    LineItem scan(String code) { return scan(code, 1); }

    LineItem scan(String code, int count) {
        Product p = lookup(code);
        if (p.unit() != PricingUnit.EACH) throw new PosException(p.name() + " is sold by weight: put it on the scale");
        return current().add(p, BigDecimal.valueOf(count));
    }

    /** Loose produce: the cashier keys the PLU and the scale sends the weight in grams. */
    LineItem weigh(String plu, int grams) {
        Product p = lookup(plu);
        if (p.unit() != PricingUnit.PER_KG) throw new PosException(p.name() + " is not sold by weight");
        if (grams <= 0) throw new PosException("Scale reading must be positive");
        return current().add(p, BigDecimal.valueOf(grams).movePointLeft(3));
    }

    void voidLine(int lineNo) { current().voidLine(lineNo); }
    void coupon(String code) { current().addCoupon(code); }
    PriceBreakdown total() { return current().beginTender(engine); }
    void backToScanning() { current().reopen(); }
    void cancelSale() { current().cancel(gateway); }

    /** Returns the receipt when the bill is fully paid, else null (more tenders needed). */
    Receipt pay(Tender t) {
        Sale s = current();
        if (!s.pay(t, gateway)) return null;
        for (SkuTotal item : s.breakdown().items()) inventory.adjust(item.product().sku(), item.quantity().negate());
        Receipt r = new Receipt(receipts.newId(), id, s.lines(), s.breakdown(), s.payments(), s.change());
        receipts.save(r);
        return r;
    }

    /** Refund what the customer actually paid per unit (after offers and coupons), not the shelf price. */
    BigDecimal refund(String receiptId, String sku, int qty) {
        Receipt r = receipts.get(receiptId);
        SkuTotal bought = r.breakdown().items().stream().filter(t -> t.product().sku().equals(sku)).findFirst()
                .orElseThrow(() -> new PosException(sku + " is not on receipt " + receiptId));
        BigDecimal q = BigDecimal.valueOf(qty);
        BigDecimal after = receipts.claimReturn(r, sku, q, bought.quantity());
        // refund = share(after) - share(before): the parts always add up to exactly what was paid
        BigDecimal net = r.breakdown().netBySku().get(sku);
        BigDecimal refund = share(net, after, bought.quantity()).subtract(share(net, after.subtract(q), bought.quantity()));
        inventory.adjust(sku, q);
        return refund;
    }

    private static BigDecimal share(BigDecimal net, BigDecimal units, BigDecimal of) {
        return net.multiply(units).divide(of, 2, RoundingMode.HALF_UP);
    }

    private Product lookup(String code) {
        if (code.length() == 13 && !Ean13.valid(code)) throw new PosException("Bad barcode " + code + ": check digit wrong, rescan");
        return catalog.find(code).orElseThrow(() -> new PosException("Unknown code " + code + ": call a supervisor"));
    }

    private Sale current() {
        if (sale == null) throw new PosException("No sale in progress");
        return sale;
    }
}

/** EAN-13 check digit: weights 1,3,1,3... on the first 12 digits; the 13th makes the sum a multiple of 10. */
final class Ean13 {
    private Ean13() { }
    static String withCheckDigit(String first12) {
        int sum = 0;
        for (int i = 0; i < 12; i++) sum += (first12.charAt(i) - '0') * (i % 2 == 0 ? 1 : 3);
        return first12 + (10 - sum % 10) % 10;
    }
    static boolean valid(String code) {
        return code.chars().allMatch(Character::isDigit) && withCheckDigit(code.substring(0, 12)).equals(code);
    }
}
