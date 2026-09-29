package lld.ch16.carrental;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

record BillItem(String description, BillItemType type, BigDecimal amount) {}

/**
 * The ONLY place that knows what a rental costs: every charge becomes a line here.
 * Every method is synchronized on the bill, so a line can never be added while it is being settled.
 */
final class Bill {
    private final List<BillItem> items = new ArrayList<>();
    private boolean paid;

    synchronized void add(BillItem item) {
        if (paid) throw new IllegalStateException("bill already settled");
        items.add(item);
    }
    synchronized List<BillItem> items() { return List.copyOf(items); }
    synchronized BigDecimal total() { return items.stream().map(BillItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add); }
    synchronized boolean isPaid() { return paid; }

    /** Idempotent: settling a bill that is already paid does not charge the member a second time. */
    synchronized boolean settle(PaymentMethod method) {
        if (paid) return true;
        paid = method.charge(total());
        return paid;
    }
}

/** Strategy for how money is taken. */
interface PaymentMethod { boolean charge(BigDecimal amount); }

record CardPayment(String cardToken) implements PaymentMethod {
    public boolean charge(BigDecimal amount) {
        System.out.println("    charged " + amount + " to card token " + cardToken);
        return true;
    }
}

record CashPayment(BigDecimal tendered) implements PaymentMethod {
    public boolean charge(BigDecimal amount) { return tendered.compareTo(amount) >= 0; }
}

/**
 * Template Method: toBillItem() is fixed; subclasses only say what kind of add-on they are
 * and may override how the price is computed (the hook).
 */
abstract class ReservationAddOn {
    private final String name;
    protected final BigDecimal price;

    protected ReservationAddOn(String name, BigDecimal price) {
        this.name = name;
        this.price = price;
    }

    final BillItem toBillItem(long rentalDays) {
        return new BillItem(kind() + ": " + name, BillItemType.ADD_ON, priceFor(rentalDays));
    }

    protected abstract String kind();

    /** Hook: by default an add-on is charged per rental day. */
    protected BigDecimal priceFor(long rentalDays) { return price.multiply(BigDecimal.valueOf(rentalDays)); }
}

final class RentalInsurance extends ReservationAddOn {
    RentalInsurance(String name, BigDecimal perDay) { super(name, perDay); }
    protected String kind() { return "Insurance"; }
}

final class Equipment extends ReservationAddOn {
    Equipment(String name, BigDecimal perDay) { super(name, perDay); }
    protected String kind() { return "Equipment"; }
}

/** A service such as roadside assistance or wifi is a flat fee per rental. */
final class Service extends ReservationAddOn {
    Service(String name, BigDecimal flatFee) { super(name, flatFee); }
    protected String kind() { return "Service"; }
    @Override protected BigDecimal priceFor(long rentalDays) { return price; }
}
