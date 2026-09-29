package lld.ch19.fooddelivery;

import java.math.BigDecimal;

public class Main {
    public static void main(String[] args) {
        FoodDeliveryService app = new FoodDeliveryService(new NearestAgentStrategy());
        app.subscribe(new Notifier());

        Restaurant dosa = new Restaurant("R1", "Dosa Corner", new Location(1, 1), 4);
        dosa.addItem(new MenuItem("D1", "Masala Dosa", new BigDecimal("120.00")));
        dosa.addItem(new MenuItem("D2", "Filter Coffee", new BigDecimal("40.00")));
        Restaurant pizza = new Restaurant("R2", "Pizza Hub", new Location(5, 5), 10);
        pizza.addItem(new MenuItem("P1", "Margherita", new BigDecimal("299.00")));
        Restaurant far = new Restaurant("R3", "Far Away Grill", new Location(30, 30), 10);
        app.addRestaurant(dosa);
        app.addRestaurant(pizza);
        app.addRestaurant(far);
        DeliveryAgent ravi = new DeliveryAgent("A1", "Ravi", new Location(0, 0));
        DeliveryAgent meena = new DeliveryAgent("A2", "Meena", new Location(6, 6));
        app.addAgent(ravi);
        app.addAgent(meena);

        Customer asha = new Customer("C1", "Asha", "98000", new Location(2, 2));
        Customer vik = new Customer("C2", "Vikram", "98001", new Location(4, 4));

        System.out.println("== 1. Browse within 10 km ==");
        app.browse(asha, 10).forEach(r -> System.out.println("  " + r.name()));

        System.out.println("\n== 2. One restaurant per cart ==");
        Cart cart = new Cart();
        cart.add(dosa, "D1", 2);
        cart.add(dosa, "D2", 1);
        try { cart.add(pizza, "P1", 1); } catch (IllegalStateException e) { System.out.println("  " + e.getMessage()); }

        System.out.println("\n== 3. Place, accept, prepare, ready, deliver ==");
        Order o1 = app.placeOrder(asha, cart, new UpiPayment("asha@upi"));
        System.out.println("  subtotal " + o1.subtotal() + " + delivery " + o1.deliveryFee() + " = " + o1.total());
        dosa.item("D1").orElseThrow().setPrice(new BigDecimal("150.00"));    // later price change...
        System.out.println("  price changed after ordering; order still " + o1.total());
        app.restaurantAccepts(o1.id());
        app.startPreparing(o1.id());
        app.foodReady(o1.id());
        System.out.println("  rider: " + o1.agent().orElseThrow() + " (nearest to Dosa Corner)");

        System.out.println("\n== 4. Kitchen capacity: 4 items max at Dosa Corner ==");
        Cart c2 = new Cart();
        c2.add(dosa, "D1", 3);
        Order o2 = app.placeOrder(vik, c2, new CashOnDelivery());
        app.restaurantAccepts(o2.id());                                     // 3 of 4 slots used
        Cart c3 = new Cart();
        c3.add(dosa, "D2", 2);
        Order o3 = app.placeOrder(asha, c3, new CardPayment("4242"));
        app.restaurantAccepts(o3.id());                                     // needs 2, only 1 free

        System.out.println("\n== 5. Only one rider free: second ready order waits ==");
        app.startPreparing(o2.id());
        app.foodReady(o2.id());                                             // Meena takes it
        Cart c4 = new Cart();
        c4.add(pizza, "P1", 1);
        Order o4 = app.placeOrder(vik, c4, new UpiPayment("vik@upi"));
        app.restaurantAccepts(o4.id());
        app.startPreparing(o4.id());
        app.foodReady(o4.id());                                             // nobody free
        app.delivered(o1.id());                                             // Ravi frees up -> takes o4
        System.out.println("  o4 rider: " + o4.agent().orElseThrow());

        System.out.println("\n== 6. Cancel rules ==");
        System.out.println("  cancel o2 (out for delivery)? " + app.cancel(o2.id()));
        Cart c5 = new Cart();
        c5.add(pizza, "P1", 2);
        Order o5 = app.placeOrder(asha, c5, new CardPayment("4242"));
        app.restaurantAccepts(o5.id());
        System.out.println("  cancel o5 (confirmed)? " + app.cancel(o5.id()));

        System.out.println("\n== 7. Swap strategy at run time: fair share ==");
        app.delivered(o2.id());
        app.delivered(o4.id());
        System.out.println("  deliveries today: Ravi=" + ravi.deliveriesToday() + ", Meena=" + meena.deliveriesToday()
                + " (both now at Vikram's address)");
        app.setStrategy(new FairShareStrategy());
        Cart c6 = new Cart();
        c6.add(dosa, "D2", 1);
        Order o6 = app.placeOrder(asha, c6, new UpiPayment("asha@upi"));
        app.restaurantAccepts(o6.id());
        app.startPreparing(o6.id());
        app.foodReady(o6.id());
        System.out.println("  o6 rider: " + o6.agent().orElseThrow() + " (fewer deliveries today)");
        System.out.println("  o1 history: " + o1.history());
    }
}
