package lld.ch04.ocp.after;

import java.math.BigDecimal;

public class BasicInvoiceGenerator implements InvoiceGenerator {
    @Override
    public void generate(BigDecimal amount) {
        System.out.println("Generating basic invoice for amount: " + amount);
    }
}
