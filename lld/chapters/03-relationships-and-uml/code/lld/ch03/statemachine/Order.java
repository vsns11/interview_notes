package lld.ch03.statemachine;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Code for the state machine diagram. Each transition is written as
 *   event [guard] / action
 * An event that has no transition from the current state is rejected.
 */
public final class Order {
    private final String id;
    private final BigDecimal total;   // money: BigDecimal, not double
    private OrderState state = OrderState.CREATED;   // initial pseudostate -> CREATED
    private final List<String> log = new ArrayList<>();

    public Order(String id, BigDecimal total) {
        this.id = id;
        this.total = total;
        log.add("[*] -> CREATED");
    }

    public OrderState state() { return state; }
    public List<String> log() { return List.copyOf(log); }

    public void handle(OrderEvent event) {
        OrderState from = state;
        OrderState to = switch (event) {
            case OrderEvent.Pay p when state == OrderState.CREATED && p.amount().compareTo(total) == 0 -> OrderState.PAID;
            case OrderEvent.Pay p when state == OrderState.CREATED ->
                    reject(event, "guard failed: paid " + p.amount() + " but total is " + total);
            case OrderEvent.Ship s when state == OrderState.PAID -> {
                log.add("  action: print label " + s.trackingId());
                yield OrderState.SHIPPED;
            }
            case OrderEvent.Deliver d when state == OrderState.SHIPPED -> OrderState.DELIVERED;
            case OrderEvent.Cancel c when state == OrderState.CREATED -> OrderState.CANCELLED;
            case OrderEvent.Cancel c when state == OrderState.PAID -> {
                log.add("  action: refund " + total);
                yield OrderState.CANCELLED;
            }
            default -> reject(event, "no transition from " + state);
        };
        state = to;
        log.add(from + " --" + event.getClass().getSimpleName() + "--> " + to);
    }

    private OrderState reject(OrderEvent e, String why) {
        throw new IllegalStateException(id + ": " + e.getClass().getSimpleName() + " rejected, " + why);
    }
}
