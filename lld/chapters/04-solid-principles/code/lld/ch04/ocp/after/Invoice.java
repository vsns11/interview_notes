package lld.ch04.ocp.after;

import java.math.BigDecimal;
import java.util.Objects;

/** Step 3: Invoice delegates. It no longer knows how many formats exist. */
public class Invoice {
    private final BigDecimal amount;
    private final InvoiceGenerator generator;

    public Invoice(BigDecimal amount, InvoiceGenerator generator) {
        this.amount = Objects.requireNonNull(amount);
        this.generator = Objects.requireNonNull(generator);
    }

    public void generate() {
        generator.generate(amount); // no branching
    }

    // Still closed? No -- a new discount rule would still edit this class.
    // That is fine until discounts actually start to vary.
    public void applyDiscount() {
        System.out.println("Applying discount on invoice: " + amount);
    }
}
