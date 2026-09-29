package lld.ch04.ocp.before;

import java.math.BigDecimal;

/** VIOLATION: every new invoice type means opening this working, tested method. */
public class Invoice {
    private final BigDecimal amount;

    public Invoice(BigDecimal amount) {
        this.amount = amount;
    }

    public void generate(String invoiceType) {
        if (invoiceType.equals("basic")) {
            System.out.println("Generating basic invoice for amount: " + amount);
        } else if (invoiceType.equals("international")) {
            System.out.println("Generating international invoice for amount: " + amount);
        } else {
            throw new IllegalArgumentException("Unknown invoice type: " + invoiceType);
        }
    }

    public void applyDiscount() {
        System.out.println("Applying discount on invoice: " + amount);
    }
}
