package lld.ch11.state;

import java.util.LinkedHashMap;
import java.util.Map;

/** CONTEXT: owns inventory and money; states rule on legality and pick the next state. */
final class VendingMachine {
    private record Slot(String product, int priceCents, int count) {}

    private final Map<String, Slot> slots = new LinkedHashMap<>();
    private VendingState state = new Idle();
    private int balance = 0;

    void load(String code, String product, int price, int count) { slots.put(code, new Slot(product, price, count)); }

    // actions: delegate to the current state
    void insertCoin(int cents) { System.out.println("insert " + cents); state.insertCoin(this, cents); }
    void select(String code)   { System.out.println("select " + code); state.select(this, code); }
    void cancel()              { System.out.println("cancel"); state.cancel(this); }

    // helpers the states call
    void setState(VendingState next) {
        System.out.println("    " + state.name() + " -> " + next.name());
        state = next;
    }
    void addBalance(int cents) { balance += cents; System.out.println("  balance " + balance); }
    int balance() { return balance; }
    boolean inStock(String code) { return slots.containsKey(code) && slots.get(code).count() > 0; }
    boolean anythingInStock() { return slots.values().stream().anyMatch(s -> s.count() > 0); }
    int priceOf(String code) { return slots.get(code).priceCents(); }

    void dispense(String code) {
        Slot s = slots.get(code);
        slots.put(code, new Slot(s.product(), s.priceCents(), s.count() - 1));
        balance -= s.priceCents();
        System.out.println("  >> dispensed " + s.product());
    }

    void returnChange() {
        if (balance > 0) System.out.println("  >> change returned " + balance);
        balance = 0;
    }
}
