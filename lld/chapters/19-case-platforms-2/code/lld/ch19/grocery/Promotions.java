package lld.ch19.grocery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Optional;
import java.util.Set;

/** One discount line on the receipt. sku == null means a basket-level discount. */
record Discount(String description, String sku, BigDecimal amount) { }

/** Strategy for an item-level offer. Called once per SKU with the total quantity of that SKU in the basket. */
interface ItemPromotion {
    Optional<Discount> evaluate(Product p, BigDecimal quantity);
}

/** "Buy 2, get 1 free": in every group of (buy + free) units, the free units cost nothing. */
record BuyXGetYFree(String sku, int buy, int free) implements ItemPromotion {
    @Override public Optional<Discount> evaluate(Product p, BigDecimal qty) {
        if (!p.sku().equals(sku)) return Optional.empty();
        int groups = qty.intValue() / (buy + free);
        if (groups == 0) return Optional.empty();
        BigDecimal off = p.unitPrice().multiply(BigDecimal.valueOf((long) groups * free));
        return Optional.of(new Discount("Buy " + buy + " get " + free + " free: " + p.name(), sku, Money.round(off)));
    }
}

/** "3 for Rs 100": each full group of n units costs a fixed price. */
record MultiBuy(String sku, int n, BigDecimal groupPrice) implements ItemPromotion {
    @Override public Optional<Discount> evaluate(Product p, BigDecimal qty) {
        if (!p.sku().equals(sku)) return Optional.empty();
        int groups = qty.intValue() / n;
        BigDecimal saving = p.unitPrice().multiply(BigDecimal.valueOf(n)).subtract(groupPrice);
        if (groups == 0 || saving.signum() <= 0) return Optional.empty();
        return Optional.of(new Discount(n + " for " + Money.fmt(groupPrice) + ": " + p.name(), sku,
                Money.round(saving.multiply(BigDecimal.valueOf(groups)))));
    }
}

/** "10% off all produce" — applies to weighed items too. */
record PercentOffDepartment(Department dept, int percent) implements ItemPromotion {
    @Override public Optional<Discount> evaluate(Product p, BigDecimal qty) {
        if (p.dept() != dept) return Optional.empty();
        BigDecimal gross = Money.round(p.unitPrice().multiply(qty));
        BigDecimal off = gross.multiply(BigDecimal.valueOf(percent)).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        return Optional.of(new Discount(percent + "% off " + dept + ": " + p.name(), p.sku(), off));
    }
}

/** Strategy for a basket-level offer, evaluated after item offers, on what the customer still owes. */
interface BasketPromotion {
    Optional<Discount> evaluate(BigDecimal subtotalAfterItemOffers, Set<String> couponsPresented);
}

/** "Rs 50 off when you spend Rs 500 or more" — needs the coupon code to be scanned. */
record SpendThresholdCoupon(String code, BigDecimal minSpend, BigDecimal amountOff) implements BasketPromotion {
    @Override public Optional<Discount> evaluate(BigDecimal subtotal, Set<String> coupons) {
        if (!coupons.contains(code) || subtotal.compareTo(minSpend) < 0) return Optional.empty();
        return Optional.of(new Discount("Coupon " + code, null, amountOff.min(subtotal)));
    }
}
