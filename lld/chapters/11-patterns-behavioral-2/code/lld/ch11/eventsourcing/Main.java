package lld.ch11.eventsourcing;

import java.math.BigDecimal;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        EventStore store = new EventStore();
        String cartId = "cart-7";
        store.append(cartId, new CartEvent.ItemAdded("book", 2, new BigDecimal("12.99")));
        store.append(cartId, new CartEvent.ItemAdded("pen", 5, new BigDecimal("0.99")));

        ShoppingCart current = ShoppingCart.replay(store.read(cartId), Integer.MAX_VALUE);
        store.append(cartId, current.decideRemove("pen"));      // validated, then recorded
        store.append(cartId, new CartEvent.CheckedOut());

        List<CartEvent> history = store.read(cartId);
        System.out.println("event log:");
        history.forEach(e -> System.out.println("  " + e));

        System.out.println("state after each event (replay = time travel):");
        for (int n = 0; n <= history.size(); n++) {
            System.out.println("  after " + n + ": " + ShoppingCart.replay(history, n));
        }

        try {
            ShoppingCart.replay(history, history.size()).decideRemove("book");
        } catch (IllegalStateException e) {
            System.out.println("command rejected: " + e.getMessage());
        }
    }
}
