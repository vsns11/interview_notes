package lld.ch04.ocp.after;

import java.math.BigDecimal;

/** Step 4: the ONLY new code for a third format. No existing class was opened. */
public class DetailedInvoiceGenerator implements InvoiceGenerator {
    @Override
    public void generate(BigDecimal amount) {
        System.out.println("Generating detailed invoice for amount: " + amount);
        System.out.println("  line items, tax breakdown and payment terms");
    }
}
