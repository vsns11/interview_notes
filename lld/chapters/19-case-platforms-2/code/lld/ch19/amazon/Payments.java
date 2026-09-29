package lld.ch19.amazon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

enum PaymentStatus { UNPAID, PENDING, COMPLETED, DECLINED, CANCELLED, REFUNDED }

/**
 * Strategy for taking money. Each subclass behaves differently: a card clears at
 * once, a bank transfer comes back PENDING and only clears later.
 */
abstract sealed class Payment permits CreditCardPayment, BankTransferPayment {
    private final BigDecimal amount;
    protected PaymentStatus status = PaymentStatus.UNPAID;

    Payment(BigDecimal amount) { this.amount = Money.of(amount); }

    BigDecimal amount() { return amount; }
    synchronized PaymentStatus status() { return status; }

    /** Try to take the money. Returns the new status. */
    abstract PaymentStatus process();

    abstract String describe();

    synchronized void markSettled() {
        if (status == PaymentStatus.PENDING) status = PaymentStatus.COMPLETED;
    }

    synchronized PaymentStatus refund() {
        if (status == PaymentStatus.COMPLETED) status = PaymentStatus.REFUNDED;
        else if (status == PaymentStatus.PENDING || status == PaymentStatus.UNPAID) status = PaymentStatus.CANCELLED;
        return status;
    }
}

final class CreditCardPayment extends Payment {
    private final String last4;             // never store the full card number
    private final BigDecimal creditLimit;

    CreditCardPayment(BigDecimal amount, String last4, BigDecimal creditLimit) {
        super(amount);
        this.last4 = last4;
        this.creditLimit = creditLimit;
    }

    @Override synchronized PaymentStatus process() {
        status = amount().compareTo(creditLimit) <= 0 ? PaymentStatus.COMPLETED : PaymentStatus.DECLINED;
        return status;
    }

    @Override String describe() { return "card ****" + last4; }
}

final class BankTransferPayment extends Payment {
    private final String bankName;

    BankTransferPayment(BigDecimal amount, String bankName) {
        super(amount);
        this.bankName = bankName;
    }

    @Override synchronized PaymentStatus process() {
        status = PaymentStatus.PENDING;      // clears later, when the bank confirms
        return status;
    }

    @Override String describe() { return "bank transfer via " + bankName; }
}

enum ShipmentStatus { PENDING, SHIPPED, DELIVERED, ON_HOLD }

record ShipmentLog(ShipmentStatus status, String location) { }

/** Keeps a log, not one status field: the last entry is "now", the rest is history. */
final class Shipment {
    private final String shipmentNumber;
    private final String method;
    private final List<ShipmentLog> log = new ArrayList<>();

    Shipment(String shipmentNumber, String method) {
        this.shipmentNumber = shipmentNumber;
        this.method = method;
        log.add(new ShipmentLog(ShipmentStatus.PENDING, "warehouse"));
    }

    String shipmentNumber() { return shipmentNumber; }
    String method() { return method; }
    synchronized void addLog(ShipmentStatus s, String where) { log.add(new ShipmentLog(s, where)); }
    synchronized ShipmentLog current() { return log.get(log.size() - 1); }
    synchronized List<ShipmentLog> history() { return List.copyOf(log); }
}
