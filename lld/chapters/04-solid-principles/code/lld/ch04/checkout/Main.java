package lld.ch04.checkout;

import java.math.BigDecimal;
import java.util.List;
import lld.ch04.checkout.fin.Checkout;
import lld.ch04.checkout.fin.EmailNotifier;
import lld.ch04.checkout.fin.InMemoryOrders;
import lld.ch04.checkout.fin.SilentNotifier;
import lld.ch04.checkout.fin.SqlOrderRepository;
import lld.ch04.checkout.pricing.CarelessFixedDiscount;
import lld.ch04.checkout.pricing.FixedDiscount;
import lld.ch04.checkout.pricing.MemberDiscount;
import lld.ch04.checkout.pricing.PricingRule;
import lld.ch04.checkout.pricing.RegularPrice;
import lld.ch04.checkout.pricing.StaffDiscount;
import lld.ch04.checkout.step0.CustomerOrder;
import lld.ch04.checkout.step0.OrderService;
import lld.ch04.checkout.step2.PriceCalculator;
import lld.ch04.checkout.step3.PricingRuleContract;

public class Main {
    private static final BigDecimal TWENTY = new BigDecimal("20.00");

    public static void main(String[] args) {
        System.out.println("=== Step 0: the original class ===");
        var legacy = new OrderService();
        legacy.process(new CustomerOrder("A-1", "regular", new BigDecimal("100.00"), "ada@example.com"));
        legacy.process(new CustomerOrder("A-2", "staff", new BigDecimal("100.00"), "grace@example.com"));

        System.out.println("=== Step 1: SRP -- split by actor (same output) ===");
        new lld.ch04.checkout.step1.Checkout()
                .process(new CustomerOrder("A-2", "staff", new BigDecimal("100.00"), "grace@example.com"));

        System.out.println("=== Step 2: OCP -- one class per pricing rule ===");
        var calc = new PriceCalculator();
        var order = new Order("A-3", new BigDecimal("100.00"), "ada@example.com");
        for (PricingRule r : List.of(new RegularPrice(), new MemberDiscount(), new StaffDiscount())) {
            System.out.println("  " + r.getClass().getSimpleName() + " -> " + calc.total(order, r));
        }

        System.out.println("=== Step 3: LSP -- every rule must keep 'total >= 0' ===");
        var small = new Order("A-4", new BigDecimal("10.00"), "ada@example.com");
        System.out.println("  careless FixedDiscount(20) on 10 -> "
                + calc.total(small, new CarelessFixedDiscount(TWENTY)));
        System.out.println("  correct  FixedDiscount(20) on 10 -> "
                + calc.total(small, new FixedDiscount(TWENTY)));
        List<PricingRule> all = List.of(new RegularPrice(), new MemberDiscount(),
                new StaffDiscount(), new FixedDiscount(TWENTY), new CarelessFixedDiscount(TWENTY));
        long passing = all.stream().filter(PricingRuleContract::holds).count();
        System.out.println("  " + passing + "/" + all.size() + " rules are safe substitutes");

        System.out.println("=== Steps 4+5: ISP + DIP -- same Checkout, two wirings ===");
        var a9 = new Order("A-9", new BigDecimal("200.00"), "ada@example.com");
        System.out.println("Production wiring:");
        var db = new SqlOrderRepository();
        new Checkout(new MemberDiscount(), db, new EmailNotifier()).process(a9);
        System.out.println("  reporting side can still find it: " + db.findById("A-9").isPresent());

        System.out.println("Test wiring, same Checkout class:");
        var fake = new InMemoryOrders();
        new Checkout(new MemberDiscount(), fake, new SilentNotifier()).process(a9);
        System.out.println("  test double recorded " + fake.saved());

        System.out.println("Lambda wiring (roles are functional interfaces):");
        new Checkout(amount -> amount, (o, t) -> System.out.println("  saved " + o.orderId()),
                (o, t) -> System.out.println("  notified " + o.email())).process(a9);
        new Checkout(new RegularPrice(), fake, new SilentNotifier())
                .process(new Order("A-10", BigDecimal.ZERO, "x@example.com"));
    }
}
