package lld.ch11.strategy;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.IntPredicate;
import java.util.function.UnaryOperator;

public class Main {
    record Product(String name, long priceCents, double rating) {}

    public static void main(String[] args) {
        System.out.println("=== 1. Retry policies: same ApiClient code, three strategies ===");
        IntPredicate succeedsOnThirdTry = attempt -> attempt >= 3;
        List<Map.Entry<String, RetryPolicy>> policies = List.of(
            Map.entry("none (a lambda)", RetryPolicy.none()),
            Map.entry("FixedDelay(100, 4)", new FixedDelay(100, 4)),
            Map.entry("ExponentialBackoff(100, 4)", new ExponentialBackoff(100, 4)));
        for (Map.Entry<String, RetryPolicy> e : policies) {
            System.out.println(e.getKey() + ":");
            new ApiClient(e.getValue(), succeedsOnThirdTry).call();
        }
        System.out.println("ExponentialBackoff(100, 4) against a server that is always down:");
        new ApiClient(new ExponentialBackoff(100, 4), attempt -> false).call();

        System.out.println("\n=== 2. Comparator: the JDK's everyday strategy ===");
        List<Product> products = new ArrayList<>(List.of(
            new Product("mouse", 1_999, 4.5), new Product("keyboard", 4_999, 4.7),
            new Product("cable", 499, 4.5), new Product("monitor", 19_999, 4.2)));
        Comparator<Product> byPrice = Comparator.comparingLong(Product::priceCents);
        Comparator<Product> byRatingThenName =
            Comparator.comparingDouble(Product::rating).reversed().thenComparing(Product::name);
        products.sort(byPrice);
        System.out.println("  by price:          " + products.stream().map(Product::name).toList());
        products.sort(byRatingThenName);
        System.out.println("  by rating, name:   " + products.stream().map(Product::name).toList());

        System.out.println("\n=== 3. Strategy registry: lambdas chosen by configuration ===");
        Map<String, UnaryOperator<BigDecimal>> pricing = Map.of(
            "regular", p -> p,
            "member", p -> p.multiply(new BigDecimal("0.90")).setScale(2, RoundingMode.HALF_EVEN),  // 10% off
            "flash-sale", p -> p.subtract(new BigDecimal("20.00")).max(BigDecimal.ZERO));          // flat 20.00 off
        BigDecimal keyboardPrice = new BigDecimal("49.99");
        for (String tier : List.of("regular", "member", "flash-sale")) {   // e.g. read from config
            UnaryOperator<BigDecimal> strategy = pricing.get(tier);
            System.out.printf("  %-10s keyboard costs %s%n", tier, strategy.apply(keyboardPrice));
        }
    }
}
