package lld.ch11.specification;

/** Named rules, in the language of the business. Each is tiny and testable alone. */
public final class ProductSpecs {
    private ProductSpecs() {}

    public static Specification<Product> inCategory(String c) { return p -> p.category().equals(c); }
    public static Specification<Product> cheaperThan(long cents) { return p -> p.priceCents() < cents; }
    public static Specification<Product> inStock() { return p -> p.stock() > 0; }
    public static Specification<Product> ratedAtLeast(double r) { return p -> p.rating() >= r; }
}
