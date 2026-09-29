package lld.ch19.foodkart;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.TreeSet;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

enum Gender { M, F, OTHER }

record User(String name, Gender gender, String phone, String pincode) { }

record Review(String userPhone, int rating, String comment) {
    Review {
        if (rating < 1 || rating > 5) throw new IllegalArgumentException("rating must be 1..5");
    }
}

record FoodOrder(int id, String userPhone, String restaurant, String item, int quantity, BigDecimal total) { }

/** A restaurant serves ONE dish to MANY pincodes. Quantity is its remaining capacity. */
final class Restaurant {
    private final String name;
    private final String ownerPhone;
    private final String foodItem;
    private final BigDecimal price;
    private volatile Set<String> pincodes;
    private int quantity;                                         // guarded by this
    private final List<Review> reviews = new CopyOnWriteArrayList<>();

    Restaurant(String name, String ownerPhone, Set<String> pincodes, String foodItem, BigDecimal price, int quantity) {
        this.name = name; this.ownerPhone = ownerPhone; this.pincodes = Set.copyOf(pincodes);
        this.foodItem = foodItem; this.price = price; this.quantity = quantity;
    }

    String name() { return name; }
    String ownerPhone() { return ownerPhone; }
    String foodItem() { return foodItem; }
    BigDecimal price() { return price; }
    Set<String> pincodes() { return pincodes; }
    void setPincodes(Set<String> p) { pincodes = Set.copyOf(p); }
    synchronized int quantity() { return quantity; }
    synchronized void addQuantity(int q) { quantity += q; }

    /** Check-and-take in one step, so two users cannot both get the last plates. */
    synchronized boolean take(int q) {
        if (q <= 0 || q > quantity) return false;
        quantity -= q;
        return true;
    }

    /** Serviceable = delivers to this pincode AND has non-zero quantity (from the problem statement). */
    boolean serviceableFor(String pincode) { return pincodes.contains(pincode) && quantity() > 0; }

    void addReview(Review r) { reviews.add(r); }

    double rating() { return reviews.stream().mapToInt(Review::rating).average().orElse(0.0); }

    String describe() { return name + ", " + String.join("/", new TreeSet<>(pincodes)) + ", " + foodItem + " - " + quantity(); }
}

/** show_restaurant(rating|price): both sorted in DESCENDING order, as the problem states. */
enum SortBy {
    RATING(Comparator.comparingDouble(Restaurant::rating).reversed()),
    PRICE(Comparator.comparing(Restaurant::price).reversed());

    final Comparator<Restaurant> comparator;
    SortBy(Comparator<Restaurant> c) { comparator = c.thenComparing(Restaurant::name); }
}

/** Strategy: when the user does not name a restaurant, which one should we pick? */
interface RestaurantSelectionStrategy {
    Optional<Restaurant> select(List<Restaurant> canFulfil);
}

final class HighestRatingStrategy implements RestaurantSelectionStrategy {
    @Override public Optional<Restaurant> select(List<Restaurant> c) {
        return c.stream().max(Comparator.comparingDouble(Restaurant::rating)
                .thenComparing(Restaurant::quantity));              // tie: the one with most capacity left
    }
}

final class LowestPriceStrategy implements RestaurantSelectionStrategy {
    @Override public Optional<Restaurant> select(List<Restaurant> c) {
        return c.stream().min(Comparator.comparing(Restaurant::price)
                .thenComparing(Comparator.comparingDouble(Restaurant::rating).reversed()));
    }
}

/** The service behind every command in the machine-coding round. In-memory only. */
final class FoodKartService {
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final Map<String, Restaurant> restaurants = new ConcurrentHashMap<>();
    private final Map<String, List<FoodOrder>> history = new ConcurrentHashMap<>();
    private final AtomicInteger orderSeq = new AtomicInteger();
    private volatile User current;                                // one logged-in user at a time

    void registerUser(String name, Gender g, String phone, String pincode) {
        if (users.putIfAbsent(phone, new User(name, g, phone, pincode)) != null)
            throw new IllegalArgumentException("phone already registered: " + phone);
    }

    void loginUser(String phone) {
        User u = users.get(phone);
        if (u == null) throw new IllegalArgumentException("unknown user " + phone);
        current = u;                                              // previous user is logged out
    }

    void registerRestaurant(String name, String pincodesSlashSeparated, String item, BigDecimal price, int qty) {
        User owner = requireLogin();
        Restaurant r = new Restaurant(name, owner.phone(), parsePincodes(pincodesSlashSeparated), item, price, qty);
        if (restaurants.putIfAbsent(name, r) != null) throw new IllegalArgumentException("restaurant exists: " + name);
    }

    String updateQuantity(String name, int toAdd) {
        Restaurant r = ownedByCurrentUser(name);
        if (toAdd <= 0) throw new IllegalArgumentException("quantity to add must be positive");
        r.addQuantity(toAdd);
        return r.describe();
    }

    String updateLocation(String name, String pincodesSlashSeparated) {
        Restaurant r = ownedByCurrentUser(name);
        r.setPincodes(parsePincodes(pincodesSlashSeparated));
        return r.describe();
    }

    void rateRestaurant(String name, int rating, String comment) {
        restaurant(name).addReview(new Review(requireLogin().phone(), rating, comment == null ? "" : comment));
    }

    List<String> showRestaurants(SortBy sortBy) {
        String pin = requireLogin().pincode();
        return restaurants.values().stream().filter(r -> r.serviceableFor(pin))
                .sorted(sortBy.comparator).map(r -> r.name() + ", " + r.foodItem()).toList();
    }

    String placeOrder(String name, int qty) {
        User u = requireLogin();
        Restaurant r = restaurants.get(name);
        if (r == null || !r.pincodes().contains(u.pincode()) || !r.take(qty)) return "Cannot place order";
        record(u, r, qty);
        return "Order Placed Successfully.";
    }

    /** Variant: the system picks the restaurant. Only those that deliver here AND have capacity for qty qualify. */
    String placeOrder(int qty, RestaurantSelectionStrategy strategy) {
        User u = requireLogin();
        List<Restaurant> canFulfil = restaurants.values().stream()
                .filter(r -> r.pincodes().contains(u.pincode()) && r.quantity() >= qty).toList();
        for (int attempt = 0; attempt < 3; attempt++) {           // retry if a racer took the capacity
            Optional<Restaurant> pick = strategy.select(canFulfil);
            if (pick.isEmpty()) return "Cannot place order";
            if (pick.get().take(qty)) {
                record(u, pick.get(), qty);
                return "Order Placed Successfully from " + pick.get().name() + ".";
            }
            canFulfil = canFulfil.stream().filter(r -> r != pick.get()).toList();
        }
        return "Cannot place order";
    }

    List<FoodOrder> orderHistory() { return List.copyOf(history.getOrDefault(requireLogin().phone(), List.of())); }

    private void record(User u, Restaurant r, int qty) {
        FoodOrder o = new FoodOrder(orderSeq.incrementAndGet(), u.phone(), r.name(), r.foodItem(), qty,
                r.price().multiply(BigDecimal.valueOf(qty)));
        history.computeIfAbsent(u.phone(), k -> new CopyOnWriteArrayList<>()).add(o);
    }

    private User requireLogin() {
        User u = current;
        if (u == null) throw new IllegalStateException("no user logged in");
        return u;
    }

    private Restaurant restaurant(String name) {
        Restaurant r = restaurants.get(name);
        if (r == null) throw new IllegalArgumentException("no restaurant " + name);
        return r;
    }

    private Restaurant ownedByCurrentUser(String name) {
        Restaurant r = restaurant(name);
        if (!r.ownerPhone().equals(requireLogin().phone())) throw new IllegalStateException("only the owner can update " + name);
        return r;
    }

    private static Set<String> parsePincodes(String s) {
        Set<String> out = new LinkedHashSet<>();
        for (String p : s.split("/")) if (!p.isBlank()) out.add(p.trim());
        if (out.isEmpty()) throw new IllegalArgumentException("at least one pincode needed");
        return out;
    }

}
