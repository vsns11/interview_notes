package lld.ch19.amazon;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws Exception {
        Admin admin = new Admin(new Account("root", "admin@shop.test", "000"));
        ProductCategory books = admin.addCategory("Books", "Printed and e-books");
        ProductCategory electronics = admin.addCategory("Electronics", "Gadgets");

        Catalog catalog = new Catalog();
        catalog.addProduct(new Product("P1", "Clean Code", books, Money.of("32.50"), "seller-9", 5));
        catalog.addProduct(new Product("P2", "Refactoring", books, Money.of("41.00"), "seller-9", 2));
        catalog.addProduct(new Product("P3", "USB-C Cable", electronics, Money.of("9.99"), "seller-4", 100));
        catalog.addProduct(new Product("P4", "Design Patterns (last copy)", books, Money.of("55.00"), "seller-2", 1));

        System.out.println("== 1. Search ==");
        System.out.println("name 'code'      -> " + catalog.searchByName("code"));
        System.out.println("name 'usb cable' -> " + catalog.searchByName("usb cable"));
        System.out.println("category 'books' -> " + catalog.searchByCategory("books"));

        Map<String, Account> accounts = new ConcurrentHashMap<>();
        OrderService orders = new OrderService(catalog);
        orders.subscribe(new NotificationService(accounts::get, List.of(new EmailChannel(), new SmsChannel())));

        System.out.println("\n== 2. Guest fills a cart, then registers (cart carries over) ==");
        Guest guest = new Guest();
        guest.addToCart(catalog.find("P1").orElseThrow(), 1);
        guest.addToCart(catalog.find("P3").orElseThrow(), 3);
        Account aliceAcc = new Account("alice", "alice@mail.test", "+1-555-0101");
        Member alice = guest.register("M1", aliceAcc);
        accounts.put(alice.id(), aliceAcc);
        alice.cart().updateQuantity("P3", 2);
        alice.cart().snapshot().forEach(i -> System.out.println("  cart: " + i.quantity() + " x " + i.name()
                + " = " + Money.fmt(i.lineTotal())));

        System.out.println("\n== 3. Checkout with a credit card ==");
        Address home = new Address("1 Main St", "Seattle", "98101", "US");
        Order o1 = orders.placeOrder(alice, home,
                amt -> new CreditCardPayment(amt, "4242", Money.of("1000.00")));
        System.out.println("  " + o1.orderNumber() + " total " + Money.fmt(o1.total()) + ", status " + o1.status()
                + ", stock of Clean Code now " + catalog.find("P1").orElseThrow().availableCount());

        System.out.println("\n== 4. Ship, track, deliver ==");
        orders.ship(o1.orderNumber(), "UPS Ground");
        orders.updateShipment(o1.orderNumber(), ShipmentStatus.SHIPPED, "Portland hub");
        orders.updateShipment(o1.orderNumber(), ShipmentStatus.DELIVERED, "front door");
        System.out.println("  tracking now: " + o1.shipment().current() + ", order " + o1.status());
        System.out.println("  cancel after delivery? " + orders.cancel(o1.orderNumber()));

        System.out.println("\n== 5. Bank transfer (pending) then cancel before shipping -> refund ==");
        alice.addToCart(catalog.find("P2").orElseThrow(), 2);
        Order o2 = orders.placeOrder(alice, home, amt -> new BankTransferPayment(amt, "Chase"));
        System.out.println("  " + o2.orderNumber() + " status " + o2.status() + ", payment " + o2.payment().status());
        orders.onPaymentSettled(o2.orderNumber());
        System.out.println("  cancel before shipping? " + orders.cancel(o2.orderNumber())
                + " -> " + o2.status() + ", payment " + o2.payment().status()
                + ", Refactoring stock back to " + catalog.find("P2").orElseThrow().availableCount());
        System.out.println("  order log: " + o2.log().stream().map(OrderLog::status).toList());

        System.out.println("\n== 6. Illegal transition is rejected by the state machine ==");
        try {
            orders.ship(o2.orderNumber(), "FedEx");
        } catch (IllegalStateException e) {
            System.out.println("  " + e.getMessage());
        }

        System.out.println("\n== 7. Review ==");
        Product cleanCode = catalog.find("P1").orElseThrow();
        cleanCode.addReview(new ProductReview(alice.id(), 5, "Changed how I name things"));
        cleanCode.addReview(new ProductReview("M7", 4, "Good, a bit long"));
        System.out.println("  Clean Code average rating = " + cleanCode.averageRating());

        System.out.println("\n== 8. Blocked member cannot buy ==");
        admin.block(alice);
        alice.addToCart(catalog.find("P3").orElseThrow(), 1);
        try {
            orders.placeOrder(alice, home, amt -> new CreditCardPayment(amt, "4242", Money.of("1000")));
        } catch (IllegalStateException e) {
            System.out.println("  rejected: " + e.getMessage());
        }
        admin.unblock(alice);
        alice.cart().clear();

        System.out.println("\n== 9. Ten members race for the last copy ==");
        OrderService quiet = new OrderService(catalog);          // no listeners: keep output short
        Product last = catalog.find("P4").orElseThrow();
        CountDownLatch start = new CountDownLatch(1);
        AtomicInteger winners = new AtomicInteger();
        AtomicInteger losers = new AtomicInteger();
        Thread[] threads = new Thread[10];
        for (int t = 0; t < threads.length; t++) {
            Member m = new Member("R" + t, new Account("racer" + t, "r" + t + "@mail.test", "-"));
            m.addToCart(last, 1);
            threads[t] = new Thread(() -> {
                try {
                    start.await();
                    quiet.placeOrder(m, home, amt -> new CreditCardPayment(amt, "1111", new BigDecimal("500")));
                    winners.incrementAndGet();
                } catch (IllegalStateException e) {
                    losers.incrementAndGet();
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            });
            threads[t].start();
        }
        start.countDown();
        for (Thread t : threads) t.join();
        System.out.println("  winners=" + winners + " losers=" + losers + " stock left=" + last.availableCount());
    }
}
