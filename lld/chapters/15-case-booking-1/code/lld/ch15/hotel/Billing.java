package lld.ch15.hotel;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum PaymentStatus { UNPAID, PENDING, COMPLETED, DECLINED, CANCELLED, REFUNDED }

record InvoiceItem(String description, BigDecimal amount) {}   // money: BigDecimal, never double

/** A bill is a list of lines; the total is always computed, so it can never disagree with the lines. */
final class Invoice {
    private final String number;
    private final List<InvoiceItem> items = new ArrayList<>();
    Invoice(String number) { this.number = number; }
    String number()                        { return number; }
    synchronized void addItem(InvoiceItem i) { items.add(i); }
    synchronized List<InvoiceItem> items()   { return List.copyOf(items); }
    synchronized BigDecimal total()          { return items.stream().map(InvoiceItem::amount).reduce(BigDecimal.ZERO, BigDecimal::add); }
}

/** Three ways to pay, which really behave differently: a check does not clear at once. */
abstract sealed class Payment permits CreditCardTransaction, CheckTransaction, CashTransaction {
    private final BigDecimal amount;
    private PaymentStatus status = PaymentStatus.UNPAID;
    Payment(BigDecimal amount) { this.amount = amount; }
    BigDecimal amount()      { return amount; }
    PaymentStatus status()   { return status; }
    protected void setStatus(PaymentStatus s) { status = s; }
    /** Take the money. Returns true when the hotel may release the guest. */
    abstract boolean initiateTransaction();
}

final class CreditCardTransaction extends Payment {
    private final String nameOnCard;
    CreditCardTransaction(BigDecimal amount, String nameOnCard) { super(amount); this.nameOnCard = nameOnCard; }
    @Override boolean initiateTransaction() { setStatus(PaymentStatus.COMPLETED); return true; }
    @Override public String toString() { return "card(" + nameOnCard + ")"; }
}

final class CheckTransaction extends Payment {
    private final String bankName, checkNumber;
    CheckTransaction(BigDecimal amount, String bankName, String checkNumber) {
        super(amount); this.bankName = bankName; this.checkNumber = checkNumber;
    }
    @Override boolean initiateTransaction() { setStatus(PaymentStatus.PENDING); return true; }   // clears in days
    @Override public String toString() { return "check(" + bankName + " #" + checkNumber + ")"; }
}

final class CashTransaction extends Payment {
    private final BigDecimal tendered;
    CashTransaction(BigDecimal amount, BigDecimal tendered) { super(amount); this.tendered = tendered; }
    @Override boolean initiateTransaction() {
        if (tendered.compareTo(amount()) < 0) { setStatus(PaymentStatus.DECLINED); return false; }
        setStatus(PaymentStatus.COMPLETED);
        return true;
    }
    BigDecimal change() { return tendered.subtract(amount()); }
    @Override public String toString() { return "cash"; }
}

/**
 * TEMPLATE METHOD. addInvoiceItem() is the fixed algorithm (describe, price, write a line);
 * each subclass fills in only description(). A new kind of charge = a new subclass, no edit to Invoice.
 */
abstract class RoomCharge {
    private final BigDecimal amount;
    RoomCharge(BigDecimal amount) { this.amount = amount; }
    protected abstract String description();                 // the one step that varies
    final void addInvoiceItem(Invoice invoice) {             // the template: final, so the order can't change
        invoice.addItem(new InvoiceItem(description(), amount));
    }
}

final class Amenity extends RoomCharge {
    private final String name;
    Amenity(String name, BigDecimal amount) { super(amount); this.name = name; }
    @Override protected String description() { return "Amenity: " + name; }
}

final class RoomService extends RoomCharge {
    private final String request;
    RoomService(String request, BigDecimal amount) { super(amount); this.request = request; }
    @Override protected String description() { return "Room service: " + request; }
}

final class KitchenService extends RoomCharge {
    private final String dish;
    KitchenService(String dish, BigDecimal amount) { super(amount); this.dish = dish; }
    @Override protected String description() { return "Kitchen: " + dish; }
}
