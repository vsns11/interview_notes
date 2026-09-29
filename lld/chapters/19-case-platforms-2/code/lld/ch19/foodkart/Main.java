package lld.ch19.foodkart;

import java.math.BigDecimal;

/** Driver: replays the sample test case from the problem statement, then the extras. */
public class Main {
    public static void main(String[] args) {
        FoodKartService fk = new FoodKartService();

        System.out.println("== Sample test case ==");
        fk.registerUser("Pralove", Gender.M, "phoneNumber-1", "HSR");
        fk.registerUser("Nitesh", Gender.M, "phoneNumber-2", "BTM");
        fk.registerUser("Vatsal", Gender.M, "phoneNumber-3", "BTM");

        fk.loginUser("phoneNumber-1");
        fk.registerRestaurant("Food Court-1", "BTM/HSR", "NI Thali", new BigDecimal("100"), 5);
        fk.registerRestaurant("Food Court-2", "BTM", "Burger", new BigDecimal("120"), 3);
        fk.loginUser("phoneNumber-2");
        fk.registerRestaurant("Food Court-3", "HSR", "SI Thali", new BigDecimal("150"), 1);
        fk.loginUser("phoneNumber-3");

        System.out.println("show_restaurant(price):");
        fk.showRestaurants(SortBy.PRICE).forEach(s -> System.out.println("  " + s));
        System.out.println("place_order(Food Court-1, 2): " + fk.placeOrder("Food Court-1", 2));
        System.out.println("place_order(Food Court-2, 7): " + fk.placeOrder("Food Court-2", 7));
        fk.rateRestaurant("Food Court-2", 3, "Good Food");
        fk.rateRestaurant("Food Court-1", 5, "Nice Food");
        System.out.println("show_restaurant(rating):");
        fk.showRestaurants(SortBy.RATING).forEach(s -> System.out.println("  " + s));

        fk.loginUser("phoneNumber-1");
        System.out.println("update_quantity(Food Court-2, 5): " + fk.updateQuantity("Food Court-2", 5));
        System.out.println("update_location(Food Court-2, BTM/HSR): " + fk.updateLocation("Food Court-2", "BTM/HSR"));

        System.out.println("\n== Extras ==");
        fk.loginUser("phoneNumber-2");
        try { fk.updateQuantity("Food Court-1", 10); } catch (IllegalStateException e) { System.out.println("non-owner update: " + e.getMessage()); }
        try { fk.rateRestaurant("Food Court-1", 6, null); } catch (IllegalArgumentException e) { System.out.println("rating 6: " + e.getMessage()); }
        fk.rateRestaurant("Food Court-1", 4, null);             // rating without a comment
        System.out.println("order from a restaurant that does not deliver to BTM: " + fk.placeOrder("Food Court-3", 1));
        fk.registerRestaurant("Food Court-4", "BTM", "Dosa", new BigDecimal("80"), 4);

        System.out.println("\n== Selection strategy variant (user in BTM, wants 4 plates) ==");
        fk.loginUser("phoneNumber-3");
        System.out.println("highest rating: " + fk.placeOrder(4, new HighestRatingStrategy()));
        System.out.println("lowest price  : " + fk.placeOrder(4, new LowestPriceStrategy()));
        System.out.println("lowest price  : " + fk.placeOrder(4, new LowestPriceStrategy()));
        System.out.println("lowest price  : " + fk.placeOrder(4, new LowestPriceStrategy()) + "  (no one has 4 left)");

        System.out.println("\n== Order history of phoneNumber-3 ==");
        fk.orderHistory().forEach(o -> System.out.println("  " + o));
    }
}
