package lld.ch04.ocp;

import java.math.BigDecimal;
import java.util.List;
import lld.ch04.ocp.after.BasicInvoiceGenerator;
import lld.ch04.ocp.after.DetailedInvoiceGenerator;
import lld.ch04.ocp.after.InternationalInvoiceGenerator;
import lld.ch04.ocp.after.Invoice;
import lld.ch04.ocp.after.InvoiceGenerator;

public class Main {
    public static void main(String[] args) {
        BigDecimal amount = new BigDecimal("120.00"); // money: BigDecimal with 2 decimals

        System.out.println("--- Before: branch per type ---");
        var old = new lld.ch04.ocp.before.Invoice(amount);
        old.generate("basic");
        old.generate("international");

        System.out.println("--- After: one class per format ---");
        List<InvoiceGenerator> generators = List.of(
                new BasicInvoiceGenerator(),
                new InternationalInvoiceGenerator(),
                new DetailedInvoiceGenerator(),
                // A lambda is also an extension: no new file, no edited file.
                a -> System.out.println("Generating pro-forma invoice for amount: " + a));
        for (InvoiceGenerator g : generators) {
            new Invoice(amount, g).generate();
        }
    }
}
