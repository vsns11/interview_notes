package lld.ch11.nullobject;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class Main {
    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    static void checkout(Customer c, BigDecimal price) {
        // No "if (customer != null)" anywhere: the Null Object answers every call safely.
        BigDecimal payable = price.multiply(BigDecimal.valueOf(100 - c.discountPercent()))
                                  .divide(HUNDRED, 2, RoundingMode.HALF_EVEN);
        System.out.printf("  %-6s pays %s%n", c.displayName(), payable);
        c.notify("thanks for your order");
    }

    public static void main(String[] args) {
        CustomerDirectory dir = new CustomerDirectory();
        System.out.println("=== Null Object ===");
        checkout(dir.find("c1"), new BigDecimal("100.00"));
        checkout(dir.find("unknown-id"), new BigDecimal("100.00"));

        System.out.println("=== Optional (absence visible in the signature) ===");
        Customer c = dir.lookup("unknown-id").orElse(GuestCustomer.INSTANCE);
        System.out.println("  lookup(unknown-id) present? " + dir.lookup("unknown-id").isPresent() + " -> using " + c.displayName());
    }
}
