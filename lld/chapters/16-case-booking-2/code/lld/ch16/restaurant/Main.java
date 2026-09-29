package lld.ch16.restaurant;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class Main {
    static MenuItem item(String id, String title, String price) { return new MenuItem(id, title, new BigDecimal(price)); }

    public static void main(String[] args) {
        var t1 = new Table("T1", 2);
        var t2 = new Table("T2", 4);
        var t3 = new Table("T3", 4);
        var t4 = new Table("T4", 6);
        var tables = List.of(t1, t2, t3, t4);

        var restaurant = new Restaurant("Spice Route");
        var branch = restaurant.open(new Branch("Downtown", tables, new RoundRobinAssignment(), new SmsNotifier()));
        branch.addChart(new TableChart("Banquet", List.of(t4)));

        var manager = new Manager("E1", "Maya");
        var reception = new Receptionist("E2", "Ravi");
        var wendy = new Waiter("E3", "Wendy");
        var cashier = new Cashier("E4", "Carl");
        for (var e : List.of(manager, reception, wendy, cashier, new Chef("E5", "Chen"), new Chef("E6", "Maria")))
            manager.hire(branch, e);

        System.out.println("1) Manager builds the menu");
        var menu = branch.menu();
        manager.addMenuItem(menu, "Starters", item("S1", "Tomato soup", "6.50"));
        manager.addMenuItem(menu, "Starters", item("S2", "Garlic bread", "4.00"));
        manager.addMenuItem(menu, "Mains", item("M1", "Margherita pizza", "12.00"));
        manager.addMenuItem(menu, "Mains", item("M2", "Pasta arrabbiata", "14.50"));
        manager.addMenuItem(menu, "Drinks", item("D1", "Lemonade", "3.00"));
        menu.sections().forEach(s -> System.out.println("   " + s.title() + ": " + s.items().stream().map(MenuItem::title).toList()));
        System.out.println("   charts: " + branch.charts().stream().map(TableChart::name).toList());

        var rs = branch.reservations();
        var seven = LocalDateTime.of(2026, 10, 9, 19, 0);
        var eight = seven.plusHours(1);
        System.out.println("2) " + reception.name() + " searches tables for 4 at 19:00: "
                + rs.search(seven, 4).stream().map(Table::id).toList());
        var asha = rs.reserve(new Customer("Asha", "555-0101"), 4, seven).orElseThrow();
        var ben = rs.reserve(new Customer("Ben", "555-0102"), 4, eight).orElseThrow();
        System.out.println("   Asha -> " + asha.table().id() + ", Ben -> " + ben.table().id() + " (T2 is held 19:00-21:00)");
        System.out.println("   party of 8 at 19:00: " + rs.reserve(new Customer("Big group", "555-0199"), 8, seven)
                .map(Reservation::id).orElse("no single table fits"));
        try { asha.updatePeopleCount(6); }
        catch (IllegalArgumentException e) { System.out.println("   Asha's party grows to 6: rejected, " + e.getMessage()); }
        rs.cancel(ben.id());
        System.out.println("   after Ben cancels, free for 4 at 20:00: " + rs.search(eight, 4).stream().map(Table::id).toList());

        System.out.println("3) Reminder job at 18:15");
        rs.sendReminders(seven.minusMinutes(45));
        rs.sendReminders(seven.minusMinutes(30));   // second run does not spam

        System.out.println("4) Asha checks in; a couple walks in");
        var table = rs.checkIn(asha.id());
        var walkIn = rs.freeForWalkIn(2).getFirst();
        System.out.println("   Asha seated at " + table.id() + ", walk-in couple seated at " + walkIn.id());

        System.out.println("5) Wendy takes two orders, one meal per seat");
        var o1 = wendy.openOrder(table);
        o1.add(1, menu.find("S1").orElseThrow(), 1);
        o1.add(1, menu.find("M1").orElseThrow(), 1);
        o1.add(2, menu.find("M2").orElseThrow(), 1);
        o1.add(2, menu.find("D1").orElseThrow(), 2);
        o1.add(3, menu.find("S2").orElseThrow(), 1);
        var o2 = wendy.openOrder(walkIn);
        o2.add(1, menu.find("M1").orElseThrow(), 2);
        var kitchen = branch.kitchen();
        var c1 = kitchen.submit(o1);
        var c2 = kitchen.submit(o2);
        System.out.println("   " + o1.id() + " -> chef " + c1.name() + ", " + o2.id() + " -> chef " + c2.name());

        System.out.println("6) Kitchen works the queues");
        c1.startNext();
        try { o1.add(4, menu.find("D1").orElseThrow(), 1); }
        catch (IllegalStateException e) { System.out.println("   rejected: " + e.getMessage()); }
        c2.startNext();
        c1.finishCurrent();
        c2.finishCurrent();

        System.out.println("7) Check for " + o1.id() + " (split by seat)");
        for (var meal : o1.meals()) {
            System.out.println("   seat " + meal.seat() + ": " + meal.items().stream()
                    .map(i -> i.quantity() + "x " + i.item().title()).toList() + " = " + meal.subtotal());
        }
        var check1 = Check.of(o1);
        System.out.println("   subtotal " + check1.subtotal() + ", tax " + check1.tax() + ", total " + check1.total());
        System.out.println("   paid cash: " + cashier.settle(check1, new CashPayment(new BigDecimal("60.00"))));
        var check2 = Check.of(o2);
        System.out.println("   " + o2.id() + " total " + check2.total() + " paid by check: " + cashier.settle(check2, new CheckPayment("First Bank", "000123")));
        System.out.println("   table statuses: " + tables.stream().map(t -> t.id() + "=" + t.status()).toList());
    }
}
