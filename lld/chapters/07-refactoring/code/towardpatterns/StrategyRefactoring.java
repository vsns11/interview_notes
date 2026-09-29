package lld.ch07.towardpatterns;

import java.util.EnumMap;
import java.util.Map;

/** Replace Conditional Logic with Strategy. */
public final class StrategyRefactoring {

    // ---------- BEFORE: a switch that every new tier must edit ----------
    static double legacyPrice(String tier, double amount) {
        switch (tier) {
            case "GOLD":   return amount * 0.80;
            case "SILVER": return amount * 0.90;
            case "NEW":    return amount > 100 ? amount - 10 : amount;
            default:       return amount;
        }
    }

    // ---------- AFTER: one small class (here a lambda) per rule ----------
    @FunctionalInterface
    interface DiscountPolicy {
        double apply(double amount);
    }

    enum Tier { GOLD, SILVER, NEW, REGULAR }

    static final class PriceCalculator {
        private final Map<Tier, DiscountPolicy> policies = new EnumMap<>(Tier.class);
        private final DiscountPolicy fallback = amount -> amount;

        PriceCalculator() {
            policies.put(Tier.GOLD, amount -> amount * 0.80);
            policies.put(Tier.SILVER, amount -> amount * 0.90);
            policies.put(Tier.NEW, amount -> amount > 100 ? amount - 10 : amount);
        }

        /** New tiers are registered, not coded into a switch (Open/Closed). */
        void register(Tier tier, DiscountPolicy policy) { policies.put(tier, policy); }

        double price(Tier tier, double amount) {
            return policies.getOrDefault(tier, fallback).apply(amount);
        }
    }

    static boolean demo() {
        PriceCalculator calc = new PriceCalculator();
        boolean same = true;
        for (Tier t : Tier.values()) {
            for (double amount : new double[] {50, 100, 250}) {
                double before = legacyPrice(t.name(), amount);
                double after = calc.price(t, amount);
                same &= before == after;
            }
        }
        System.out.println("Strategy:        GOLD 250 -> " + calc.price(Tier.GOLD, 250)
                + ", NEW 250 -> " + calc.price(Tier.NEW, 250) + " | same as switch: " + same);
        return same;
    }
}
