package lld.ch07.towardpatterns;

/** Replace State-Altering Conditionals with State. */
public final class StateRefactoring {

    // ---------- BEFORE: every method re-checks a status string ----------
    static final class LegacyOrder {
        String status = "NEW";

        String pay() {
            if (status.equals("NEW")) { status = "PAID"; return "paid"; }
            return "cannot pay when " + status;
        }
        String ship() {
            if (status.equals("PAID")) { status = "SHIPPED"; return "shipped"; }
            return "cannot ship when " + status;
        }
        String cancel() {
            if (status.equals("NEW") || status.equals("PAID")) { status = "CANCELLED"; return "cancelled"; }
            return "cannot cancel when " + status;
        }
    }

    // ---------- AFTER: each state is an object that knows its own legal moves ----------
    interface OrderState {
        String name();
        default OrderState pay(Order o)    { return o.reject("pay", this); }
        default OrderState ship(Order o)   { return o.reject("ship", this); }
        default OrderState cancel(Order o) { return o.reject("cancel", this); }
    }

    enum States implements OrderState {
        NEW {
            @Override public OrderState pay(Order o)    { o.log("paid"); return PAID; }
            @Override public OrderState cancel(Order o) { o.log("cancelled"); return CANCELLED; }
        },
        PAID {
            @Override public OrderState ship(Order o)   { o.log("shipped"); return SHIPPED; }
            @Override public OrderState cancel(Order o) { o.log("cancelled"); return CANCELLED; }
        },
        SHIPPED, CANCELLED
    }

    static final class Order {
        private OrderState state = States.NEW;
        private String lastMessage = "";

        String pay()    { state = state.pay(this);    return lastMessage; }
        String ship()   { state = state.ship(this);   return lastMessage; }
        String cancel() { state = state.cancel(this); return lastMessage; }

        void log(String msg) { lastMessage = msg; }

        OrderState reject(String action, OrderState current) {
            lastMessage = "cannot " + action + " when " + current.name();
            return current;                       // stay in the same state
        }
    }

    static boolean demo() {
        LegacyOrder before = new LegacyOrder();
        Order after = new Order();
        String[] script = {"ship", "pay", "pay", "ship", "cancel"};
        boolean same = true;
        StringBuilder trace = new StringBuilder();
        for (String step : script) {
            String b = switch (step) { case "pay" -> before.pay(); case "ship" -> before.ship(); default -> before.cancel(); };
            String a = switch (step) { case "pay" -> after.pay();  case "ship" -> after.ship();  default -> after.cancel(); };
            same &= a.equals(b);
            trace.append(a).append("; ");
        }
        System.out.println("State:           " + trace + "| same as if-chains: " + same);
        return same;
    }
}
