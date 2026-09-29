package lld.ch04.checkout.step3;

import java.math.BigDecimal;
import java.util.List;
import lld.ch04.checkout.pricing.PricingRule;

/** STEP 3: ONE test written against the interface, run for EVERY implementation. */
public final class PricingRuleContract {
    private static final List<BigDecimal> SAMPLE_AMOUNTS = List.of(
            new BigDecimal("0.00"), new BigDecimal("10.00"),
            new BigDecimal("100.00"), new BigDecimal("250.00"));

    private PricingRuleContract() { }

    public static boolean holds(PricingRule rule) {
        for (BigDecimal amount : SAMPLE_AMOUNTS) {
            BigDecimal total = rule.apply(amount);
            if (total.signum() < 0) {
                System.out.println("  " + rule.getClass().getSimpleName()
                        + " broke the contract: apply(" + amount + ") = " + total);
                return false;
            }
        }
        System.out.println("  " + rule.getClass().getSimpleName() + " keeps the contract");
        return true;
    }
}
