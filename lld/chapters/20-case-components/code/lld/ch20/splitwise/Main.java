package lld.ch20.splitwise;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        Splitwise app = new Splitwise();
        for (String n : List.of("Alice", "Bob", "Carol", "Dave")) app.addUser(n, n.toLowerCase() + "@mail.com");
        Group trip = app.createGroup("Goa trip", "alice", "bob", "carol", "dave");

        System.out.println("== 1. Equal split with a spare cent");
        Expense hotel = app.addExpense(trip.id(), "Hotel", "alice", Money.of("100.00"),
                List.of("alice", "bob", "carol"), new EqualSplit());
        System.out.println("Hotel shares: " + hotel.shares());

        System.out.println("== 2. Exact split");
        app.addExpense(trip.id(), "Dinner", "bob", Money.of("60.00"), List.of("alice", "bob", "carol"),
                new ExactSplit(Map.of("alice", Money.of("10.00"), "bob", Money.of("20.00"), "carol", Money.of("30.00"))));

        System.out.println("== 3. Percent split");
        Expense taxi = app.addExpense(trip.id(), "Taxi", "carol", Money.of("50.00"), List.of("alice", "bob", "dave"),
                new PercentSplit(Map.of("alice", new BigDecimal("33.33"), "bob", new BigDecimal("33.33"),
                        "dave", new BigDecimal("33.34"))));
        System.out.println("Taxi shares: " + taxi.shares());

        System.out.println("== 4. Invalid expenses are refused, ledger untouched");
        tryIt(() -> app.addExpense(trip.id(), "Snacks", "dave", Money.of("30.00"), List.of("alice", "dave"),
                new ExactSplit(Map.of("alice", Money.of("10.00"), "dave", Money.of("5.00")))));
        tryIt(() -> app.addExpense(trip.id(), "Coffee", "dave", Money.of("4.00"), List.of("dave"), new EqualSplit()));
        tryIt(() -> app.addExpense(trip.id(), "Boat", "dave", Money.of("50.00"), List.of("alice", "dave"),
                new PercentSplit(Map.of("alice", new BigDecimal("60"), "dave", new BigDecimal("30")))));

        System.out.println("== 5. Who owes whom");
        app.printLedger();
        for (String u : List.of("alice", "bob", "carol", "dave")) System.out.println("  net " + u + " = " + app.net(u));
        System.out.println("  bob owes alice: " + app.owes("bob", "alice"));

        System.out.println("== 6. Simplify the group's debts (min cash flow, greedy)");
        List<Payment> plan = app.simplifyAndApply(trip.id());
        plan.forEach(p -> System.out.println("  suggested: " + p));
        app.printLedger();
        for (String u : List.of("alice", "bob", "carol", "dave")) System.out.println("  net " + u + " = " + app.net(u));

        System.out.println("== 7. Settle up");
        tryIt(() -> app.settleUp("dave", "alice", Money.of("999.00")));
        app.settleUp("bob", "alice", Money.of("5.00"));
        System.out.println("  after a partial payment of 5.00, bob owes alice: " + app.owes("bob", "alice"));
        app.settleUp("bob", "alice", app.owes("bob", "alice"));
        for (Payment p : plan) if (!p.from().equals("bob")) app.settleUp(p.from(), p.to(), p.amount());
        app.printLedger();

        System.out.println("== 8. Group history");
        app.history(trip.id()).forEach(h -> System.out.println("  " + h));
    }

    private static void tryIt(Runnable r) {
        try {
            r.run();
            System.out.println("  accepted");
        } catch (IllegalArgumentException e) {
            System.out.println("  refused: " + e.getMessage());
        }
    }
}
