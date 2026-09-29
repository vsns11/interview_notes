package lld.ch19.grocery;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

/** One scan. The product (and so its price) is copied in at scan time; later reprices do not change it. */
record LineItem(int lineNo, Product product, BigDecimal quantity, BigDecimal gross, boolean voided) {
    LineItem asVoided() { return new LineItem(lineNo, product, quantity, gross, true); }
}

enum SaleStatus {
    OPEN, TENDERING, COMPLETED, CANCELLED;

    boolean canMoveTo(SaleStatus next) {
        return switch (this) {
            case OPEN -> next == TENDERING || next == CANCELLED;
            case TENDERING -> next == COMPLETED || next == OPEN || next == CANCELLED;
            case COMPLETED, CANCELLED -> false;
        };
    }
}

/** How the customer pays. Sealed: the compiler knows every kind of tender. */
sealed interface Tender permits Cash, Card, Upi { BigDecimal amount(); }
record Cash(BigDecimal amount) implements Tender { }
record Card(String last4, BigDecimal amount) implements Tender { }
record Upi(String vpa, BigDecimal amount) implements Tender { }

/** applied = the part of the tender that went towards the bill (cash can be more; the rest is change). */
record Payment(Tender tender, BigDecimal applied, String authCode) { }

/** Strategy / port to the card and UPI networks. Cash needs no gateway. */
interface PaymentGateway {
    String authorize(Tender t);          // returns an auth code or throws PosException("declined ...")
    void refund(String authCode, BigDecimal amount);
}

/** One customer's basket at one register, from first scan to receipt. */
final class Sale {
    final String id;
    private final List<LineItem> lines = new ArrayList<>();
    private final Set<String> coupons = new LinkedHashSet<>();
    private final List<Payment> payments = new ArrayList<>();
    private SaleStatus status = SaleStatus.OPEN;
    private PriceBreakdown frozen;       // prices are frozen when payment starts
    private BigDecimal change = Money.ZERO;

    Sale(String id) { this.id = id; }

    SaleStatus status() { return status; }
    List<LineItem> lines() { return List.copyOf(lines); }
    List<Payment> payments() { return List.copyOf(payments); }
    Set<String> coupons() { return Set.copyOf(coupons); }
    BigDecimal change() { return change; }
    PriceBreakdown breakdown() { return frozen; }

    LineItem add(Product p, BigDecimal qty) {
        require(SaleStatus.OPEN, "scan");
        if (qty.signum() <= 0) throw new PosException("Quantity must be positive");
        LineItem l = new LineItem(lines.size() + 1, p, qty, Money.round(p.unitPrice().multiply(qty)), false);
        lines.add(l);
        return l;
    }

    void voidLine(int lineNo) {
        require(SaleStatus.OPEN, "void a line");
        if (lineNo < 1 || lineNo > lines.size()) throw new PosException("No line " + lineNo);
        LineItem l = lines.get(lineNo - 1);
        if (l.voided()) throw new PosException("Line " + lineNo + " is already void");
        lines.set(lineNo - 1, l.asVoided());          // keep the line: the audit trail shows the void
    }

    void addCoupon(String code) { require(SaleStatus.OPEN, "add a coupon"); coupons.add(code); }

    PriceBreakdown beginTender(PricingEngine engine) {
        require(SaleStatus.OPEN, "start payment");
        if (lines.stream().allMatch(LineItem::voided)) throw new PosException("Nothing to pay for");
        frozen = engine.price(lines, coupons);
        move(SaleStatus.TENDERING);
        return frozen;
    }

    /** Back to scanning ("I forgot the milk") — only while nothing has been paid. */
    void reopen() {
        if (!payments.isEmpty()) throw new PosException("Payments already taken; cancel them first");
        frozen = null;
        move(SaleStatus.OPEN);
    }

    BigDecimal due() {
        BigDecimal paid = payments.stream().map(Payment::applied).reduce(Money.ZERO, BigDecimal::add);
        return frozen.total().subtract(paid);
    }

    /** Returns true when the bill is fully paid and the sale is complete. */
    boolean pay(Tender t, PaymentGateway gateway) {
        require(SaleStatus.TENDERING, "pay");
        BigDecimal due = due();
        if (t.amount().signum() <= 0) throw new PosException("Tender must be positive");
        String auth = "CASH";
        BigDecimal applied = t.amount();
        if (t instanceof Cash) {
            if (t.amount().compareTo(due) > 0) { change = t.amount().subtract(due); applied = due; }
        } else {
            if (t.amount().compareTo(due) > 0) throw new PosException("Card/UPI cannot be more than the amount due " + Money.fmt(due));
            auth = gateway.authorize(t);                 // throws if declined: nothing recorded
        }
        payments.add(new Payment(t, applied, auth));
        if (due().signum() == 0) { move(SaleStatus.COMPLETED); return true; }
        return false;
    }

    /** Cancel the whole sale; card/UPI payments already taken are reversed. */
    void cancel(PaymentGateway gateway) {
        if (!status.canMoveTo(SaleStatus.CANCELLED)) throw new PosException("Cannot cancel a " + status + " sale");
        for (Payment p : payments) if (!(p.tender() instanceof Cash)) gateway.refund(p.authCode(), p.applied());
        move(SaleStatus.CANCELLED);
    }

    private void require(SaleStatus s, String action) {
        if (status != s) throw new PosException("Cannot " + action + " when the sale is " + status);
    }

    private void move(SaleStatus next) {
        if (!status.canMoveTo(next)) throw new PosException(status + " -> " + next + " not allowed");
        status = next;
    }
}
