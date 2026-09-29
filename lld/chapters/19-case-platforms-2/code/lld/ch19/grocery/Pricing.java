package lld.ch19.grocery;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/** Strategy: are shelf prices tax-inclusive (India, EU, UK) or is tax added at the till (US sales tax)? */
interface TaxPolicy {
    BigDecimal tax(BigDecimal net, BigDecimal ratePercent);
    boolean addedOnTop();
}

final class TaxInclusive implements TaxPolicy {
    /** The price already contains the tax: tax = price x r / (100 + r). */
    @Override public BigDecimal tax(BigDecimal net, BigDecimal r) {
        return net.multiply(r).divide(BigDecimal.valueOf(100).add(r), 2, RoundingMode.HALF_UP);
    }
    @Override public boolean addedOnTop() { return false; }
}

final class TaxExclusive implements TaxPolicy {
    @Override public BigDecimal tax(BigDecimal net, BigDecimal r) {
        return net.multiply(r).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }
    @Override public boolean addedOnTop() { return true; }
}

/** All scanned lines of one SKU added together. */
record SkuTotal(Product product, BigDecimal quantity, BigDecimal gross) { }

/** Everything the receipt needs. netBySku = what the customer really paid per SKU, after every discount. */
record PriceBreakdown(List<SkuTotal> items, List<Discount> discounts, Map<String, BigDecimal> netBySku,
                      BigDecimal gross, BigDecimal discountTotal, Map<TaxCategory, BigDecimal> taxByCategory,
                      BigDecimal total) { }

/**
 * Pure function of (lines, coupons): no state, so every register can share one engine and
 * the same basket always prices the same way. Rules:
 *  1. each SKU gets at most ONE item offer — the best one for the customer;
 *  2. then the best basket coupon, on what is still owed;
 *  3. basket discounts are spread over SKUs in proportion to their value (needed for tax and refunds);
 *  4. tax is computed per tax category on the discounted amounts.
 */
final class PricingEngine {
    private final List<ItemPromotion> itemOffers;
    private final List<BasketPromotion> basketOffers;
    private final TaxPolicy taxPolicy;

    PricingEngine(List<ItemPromotion> itemOffers, List<BasketPromotion> basketOffers, TaxPolicy taxPolicy) {
        this.itemOffers = List.copyOf(itemOffers);
        this.basketOffers = List.copyOf(basketOffers);
        this.taxPolicy = taxPolicy;
    }

    PriceBreakdown price(List<LineItem> lines, Set<String> coupons) {
        Map<String, SkuTotal> bySku = new LinkedHashMap<>();
        for (LineItem l : lines) {
            if (l.voided()) continue;
            bySku.merge(l.product().sku(), new SkuTotal(l.product(), l.quantity(), l.gross()),
                    (a, b) -> new SkuTotal(a.product(), a.quantity().add(b.quantity()), a.gross().add(b.gross())));
        }
        List<Discount> discounts = new ArrayList<>();
        Map<String, BigDecimal> net = new LinkedHashMap<>();
        for (SkuTotal t : bySku.values()) {
            Optional<Discount> best = itemOffers.stream()
                    .map(p -> p.evaluate(t.product(), t.quantity()))
                    .flatMap(Optional::stream)
                    .max(Comparator.comparing(Discount::amount));
            BigDecimal off = best.map(d -> d.amount().min(t.gross())).orElse(Money.ZERO);
            best.ifPresent(d -> discounts.add(new Discount(d.description(), d.sku(), off)));
            net.put(t.product().sku(), t.gross().subtract(off));
        }
        BigDecimal subtotal = sum(net.values());
        Optional<Discount> basket = basketOffers.stream()
                .map(b -> b.evaluate(subtotal, coupons))
                .flatMap(Optional::stream)
                .max(Comparator.comparing(Discount::amount));
        basket.ifPresent(d -> { discounts.add(d); spread(d.amount(), net); });

        Map<TaxCategory, BigDecimal> netByTax = new EnumMap<>(TaxCategory.class);
        bySku.values().forEach(t -> netByTax.merge(t.product().tax(), net.get(t.product().sku()), BigDecimal::add));
        Map<TaxCategory, BigDecimal> taxByCategory = new EnumMap<>(TaxCategory.class);
        netByTax.forEach((cat, amt) -> taxByCategory.put(cat, taxPolicy.tax(amt, cat.ratePercent)));

        BigDecimal gross = sum(bySku.values().stream().map(SkuTotal::gross).toList());
        BigDecimal netTotal = sum(net.values());
        BigDecimal total = taxPolicy.addedOnTop() ? netTotal.add(sum(taxByCategory.values())) : netTotal;
        return new PriceBreakdown(List.copyOf(bySku.values()), List.copyOf(discounts), Map.copyOf(net),
                gross, gross.subtract(netTotal), taxByCategory, total);
    }

    /** Proportional allocation; the rounding remainder goes to the largest SKU so the parts add up exactly. */
    private static void spread(BigDecimal amount, Map<String, BigDecimal> net) {
        BigDecimal base = sum(net.values());
        if (base.signum() == 0) return;
        BigDecimal given = Money.ZERO;
        String largest = null;
        for (var e : net.entrySet()) {
            BigDecimal share = amount.multiply(e.getValue()).divide(base, 2, RoundingMode.HALF_UP);
            e.setValue(e.getValue().subtract(share));
            given = given.add(share);
            if (largest == null || e.getValue().compareTo(net.get(largest)) > 0) largest = e.getKey();
        }
        net.merge(largest, given.subtract(amount), BigDecimal::add);
    }

    private static BigDecimal sum(Iterable<BigDecimal> xs) {
        BigDecimal s = Money.ZERO;
        for (BigDecimal x : xs) s = s.add(x);
        return s;
    }
}
