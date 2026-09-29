package lld.ch16.restaurant;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

abstract class Employee {
    private final String id;
    private final String name;
    protected Employee(String id, String name) { this.id = id; this.name = name; }
    String id() { return id; }
    String name() { return name; }
}

final class Receptionist extends Employee {
    Receptionist(String id, String name) { super(id, name); }
}

/** A waiter opens orders and is told (Observer) when a dish is ready. */
final class Waiter extends Employee implements OrderListener {
    private static final AtomicInteger SEQ = new AtomicInteger();
    Waiter(String id, String name) { super(id, name); }

    Order openOrder(Table t) {
        t.setStatus(TableStatus.OCCUPIED);
        var o = new Order("O" + SEQ.incrementAndGet(), t);
        o.subscribe(this);
        return o;
    }

    public void onStatusChange(Order o, OrderStatus s) {
        if (s == OrderStatus.READY) System.out.println("    [" + name() + "'s pager] order " + o.id() + " for table " + o.table().id() + " is READY");
    }
}

final class Manager extends Employee {
    Manager(String id, String name) { super(id, name); }
    void addMenuItem(Menu menu, String section, MenuItem item) { menu.section(section).add(item); }
    void hire(Branch b, Employee e) { b.hire(e); }
}

/** The bill ("check"). It is computed from the order's lines, never stored separately. */
record Check(Order order, BigDecimal subtotal, BigDecimal tax, BigDecimal total) {
    static final BigDecimal TAX_RATE = new BigDecimal("0.08");
    static Check of(Order o) {
        var sub = o.subtotal();
        var tax = sub.multiply(TAX_RATE).setScale(2, RoundingMode.HALF_UP);
        return new Check(o, sub, tax, sub.add(tax));
    }
}

/** Strategy: three ways to pay, three behaviours. */
sealed interface Payment permits CardPayment, CashPayment, CheckPayment { PaymentStatus pay(BigDecimal amount); }

record CardPayment(String cardToken) implements Payment {
    public PaymentStatus pay(BigDecimal amount) { return PaymentStatus.COMPLETED; }
}

record CashPayment(BigDecimal tendered) implements Payment {
    public PaymentStatus pay(BigDecimal amount) {
        if (tendered.compareTo(amount) < 0) return PaymentStatus.DECLINED;
        System.out.println("    change due: " + tendered.subtract(amount));
        return PaymentStatus.COMPLETED;
    }
}

/** A paper check has to clear at the bank, so it is not money yet. */
record CheckPayment(String bank, String checkNumber) implements Payment {
    public PaymentStatus pay(BigDecimal amount) { return PaymentStatus.PENDING; }
}

final class Cashier extends Employee {
    Cashier(String id, String name) { super(id, name); }

    PaymentStatus settle(Check check, Payment payment) {
        // Check the order state BEFORE taking money: a refused state change after payment would leave the guest charged.
        if (check.order().status() != OrderStatus.READY)
            throw new IllegalStateException("order " + check.order().id() + " is " + check.order().status() + ", not READY");
        var status = payment.pay(check.total());
        if (status != PaymentStatus.DECLINED) {
            check.order().moveTo(OrderStatus.SERVED);
            check.order().table().setStatus(TableStatus.FREE);
        }
        return status;
    }
}

/** A branch owns its menu, kitchen and charts; staff are associated (they can move branches). */
final class Branch {
    private final String name;
    private final Menu menu = new Menu();
    private final Kitchen kitchen;
    private final List<TableChart> charts = new ArrayList<>();
    private final List<Employee> staff = new ArrayList<>();
    private final ReservationService reservations;

    Branch(String name, List<Table> tables, ChefAssignment assignment, Notifier notifier) {
        this.name = name;
        this.kitchen = new Kitchen(assignment);
        this.reservations = new ReservationService(tables, notifier);
        charts.add(new TableChart("Default", tables));
    }
    String name() { return name; }
    Menu menu() { return menu; }
    Kitchen kitchen() { return kitchen; }
    ReservationService reservations() { return reservations; }
    synchronized void addChart(TableChart c) { charts.add(c); }
    synchronized List<TableChart> charts() { return List.copyOf(charts); }
    synchronized void hire(Employee e) {
        staff.add(e);
        if (e instanceof Chef c) kitchen.addChef(c);
    }
    synchronized List<Employee> staff() { return List.copyOf(staff); }
}

final class Restaurant {
    private final String name;
    private final List<Branch> branches = new ArrayList<>();
    Restaurant(String name) { this.name = name; }
    Branch open(Branch b) { branches.add(b); return b; }
    String name() { return name; }
}
