package lld.ch19.amazon;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

/** A category such as "Books". Products point at it; it does not own them. */
record ProductCategory(String name, String description) { }

/** A review names its reviewer by id, so Product never depends on Member. */
record ProductReview(String reviewerId, int rating, String text) {
    ProductReview {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1..5");
    }
}

/** The thing being sold. Owns its reviews and guards its own stock count. */
final class Product {
    private final String id;
    private final String name;
    private final ProductCategory category;
    private final String sellerId;
    private volatile BigDecimal price;
    private int availableCount;                       // guarded by this
    private final List<ProductReview> reviews = new CopyOnWriteArrayList<>();

    Product(String id, String name, ProductCategory category, BigDecimal price,
            String sellerId, int availableCount) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.price = Money.of(price);
        this.sellerId = sellerId;
        this.availableCount = availableCount;
    }

    String id() { return id; }
    String name() { return name; }
    ProductCategory category() { return category; }
    String sellerId() { return sellerId; }
    BigDecimal price() { return price; }
    synchronized int availableCount() { return availableCount; }

    void updatePrice(BigDecimal newPrice) {
        if (newPrice.signum() < 0) throw new IllegalArgumentException("negative price");
        price = Money.of(newPrice);
    }

    /** Take stock out of the pool. False means "not enough" - nothing changes. */
    synchronized boolean reserve(int quantity) {
        if (quantity <= 0 || quantity > availableCount) return false;
        availableCount -= quantity;
        return true;
    }

    synchronized void release(int quantity) { availableCount += quantity; }

    void addReview(ProductReview review) { reviews.add(review); }

    double averageRating() {
        return reviews.stream().mapToInt(ProductReview::rating).average().orElse(0.0);
    }

    @Override public String toString() { return name + " (" + Money.fmt(price) + ")"; }
}

/** Requirement: search by name or by category. */
interface Search {
    List<Product> searchByName(String text);
    List<Product> searchByCategory(String categoryName);
}

/**
 * The catalog points to products but never owns them. It keeps one index per search
 * (words of the name, category name), so a lookup is a map read, not a scan of every
 * product. Every index is updated in one method (addProduct) so they can never drift apart.
 */
final class Catalog implements Search {
    private final Map<String, Product> byId = new ConcurrentHashMap<>();
    private final Map<String, Set<Product>> byNameWord = new ConcurrentHashMap<>();
    private final Map<String, List<Product>> byCategory = new ConcurrentHashMap<>();

    void addProduct(Product p) {
        byId.put(p.id(), p);
        for (String word : words(p.name()))
            byNameWord.computeIfAbsent(word, k -> ConcurrentHashMap.newKeySet()).add(p);
        byCategory.computeIfAbsent(key(p.category().name()), k -> new CopyOnWriteArrayList<>()).add(p);
    }

    Optional<Product> find(String productId) { return Optional.ofNullable(byId.get(productId)); }

    /** Products whose name contains every word of the query (e.g. "clean code"). */
    @Override public List<Product> searchByName(String text) {
        List<String> query = words(text);
        if (query.isEmpty()) return List.of();
        Set<Product> hits = new HashSet<>(byNameWord.getOrDefault(query.get(0), Set.of()));
        for (String w : query.subList(1, query.size())) hits.retainAll(byNameWord.getOrDefault(w, Set.of()));
        List<Product> out = new ArrayList<>(hits);
        out.sort((a, b) -> a.name().compareTo(b.name()));
        return out;
    }

    @Override public List<Product> searchByCategory(String categoryName) {
        return Collections.unmodifiableList(byCategory.getOrDefault(key(categoryName), List.of()));
    }

    private static List<String> words(String s) {
        return Arrays.stream(key(s).split("[^\\p{Alnum}]+")).filter(w -> !w.isEmpty()).toList();
    }

    private static String key(String s) { return s.toLowerCase(Locale.ROOT).trim(); }
}
