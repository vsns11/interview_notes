package lld.ch11.eventsourcing;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Aggregate whose current state is DERIVED by replaying events. */
public final class ShoppingCart {
    private final Map<String, BigDecimal> lineTotals = new LinkedHashMap<>();
    private boolean checkedOut;

    /** Rebuild state from history (optionally only the first n events: "time travel"). */
    public static ShoppingCart replay(List<CartEvent> history, int n) {
        ShoppingCart cart = new ShoppingCart();
        history.stream().limit(n).forEach(cart::apply);
        return cart;
    }

    /** Pure state transition: no validation, no side effects. Events are facts. */
    private void apply(CartEvent e) {
        switch (e) {
            case CartEvent.ItemAdded a -> lineTotals.merge(a.sku(), a.unitPrice().multiply(BigDecimal.valueOf(a.qty())), BigDecimal::add);
            case CartEvent.ItemRemoved r -> lineTotals.remove(r.sku());
            case CartEvent.CheckedOut c -> checkedOut = true;
        }
    }

    /** Command side: validate against current state, THEN emit an event. */
    public CartEvent decideRemove(String sku) {
        if (checkedOut) throw new IllegalStateException("cart already checked out");
        if (!lineTotals.containsKey(sku)) throw new IllegalArgumentException(sku + " not in cart");
        return new CartEvent.ItemRemoved(sku);
    }

    @Override
    public String toString() {
        BigDecimal total = lineTotals.values().stream().reduce(new BigDecimal("0.00"), BigDecimal::add);
        return lineTotals.keySet() + " total=" + total + (checkedOut ? " CHECKED-OUT" : "");
    }
}
