package lld.ch04.ocp.after;

import java.math.BigDecimal;

/** Step 1: the part that varies (the invoice format) gets its own abstraction. */
@FunctionalInterface
public interface InvoiceGenerator {
    void generate(BigDecimal amount);
}
