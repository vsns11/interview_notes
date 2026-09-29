package lld.ch11.specification;

import static lld.ch11.specification.ProductSpecs.*;

import java.util.List;

public class Main {
    static List<String> select(List<Product> catalog, Specification<Product> spec) {
        return catalog.stream().filter(spec::isSatisfiedBy).map(Product::name).toList();
    }

    public static void main(String[] args) {
        List<Product> catalog = List.of(
            new Product("usb-cable", "electronics", 499, 120, 4.1),
            new Product("headphones", "electronics", 7_999, 0, 4.6),
            new Product("keyboard", "electronics", 4_999, 8, 4.7),
            new Product("novel", "books", 1_299, 30, 4.8),
            new Product("cookbook", "books", 2_499, 0, 3.9));

        // The same rule objects are reused for querying AND for validation.
        Specification<Product> bargainElectronics = inCategory("electronics").and(cheaperThan(5_000)).and(inStock());
        Specification<Product> recommendable = ratedAtLeast(4.5).and(inStock());
        Specification<Product> needsRestock = inStock().not();

        System.out.println("  bargain electronics: " + select(catalog, bargainElectronics));
        System.out.println("  recommendable:       " + select(catalog, recommendable));
        System.out.println("  needs restock:       " + select(catalog, needsRestock));
        System.out.println("  books or top-rated:  " + select(catalog, inCategory("books").or(ratedAtLeast(4.7))));
        System.out.println("  is 'novel' recommendable? " + recommendable.isSatisfiedBy(catalog.get(3)));
    }
}
