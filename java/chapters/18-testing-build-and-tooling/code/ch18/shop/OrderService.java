package ch18.shop;

import java.math.BigDecimal;
import java.time.Clock;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Business logic with its collaborators passed in (dependency injection by constructor):
 * the gateway, the repository, the clock and the id generator can all be replaced in tests.
 */
public final class OrderService {
    private static final Logger LOG = Logger.getLogger(OrderService.class.getName());

    private final PaymentGateway gateway;
    private final OrderRepository repository;
    private final Clock clock;
    private final Supplier<String> ids;

    public OrderService(PaymentGateway gateway, OrderRepository repository, Clock clock, Supplier<String> ids) {
        this.gateway = gateway;
        this.repository = repository;
        this.clock = clock;
        this.ids = ids;
    }

    public Order checkout(String customer, Cart cart, String discountCode) {
        if (cart.isEmpty()) throw new IllegalStateException("cart is empty");
        BigDecimal subtotal = cart.subtotal();
        int percent = Discounts.percentFor(discountCode, subtotal);
        BigDecimal total = Discounts.apply(subtotal, percent);

        PaymentGateway.PaymentResult result = gateway.charge(customer, total);
        if (!result.approved()) {
            LOG.warning(() -> "declined: customer=" + customer + " total=" + total);
            throw new PaymentDeclinedException(customer, total);
        }
        Order order = new Order(ids.get(), customer, total, clock.instant());
        repository.save(order);
        LOG.info(() -> "order placed: id=" + order.id() + " total=" + total + " ref=" + result.reference());
        return order;
    }
}
