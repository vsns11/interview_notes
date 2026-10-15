package lld.ch11.architecture.app;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;
import lld.ch11.architecture.domain.Order;
import lld.ch11.architecture.domain.OrderEvent;
import lld.ch11.architecture.domain.OrderLine;
import lld.ch11.architecture.port.EventPublisher;
import lld.ch11.architecture.port.OrderReceipt;
import lld.ch11.architecture.port.OrderRepository;
import lld.ch11.architecture.port.OrderingUseCase;
import lld.ch11.architecture.port.PaymentGateway;
import lld.ch11.architecture.port.PlaceOrderCommand;
import lld.ch11.architecture.port.ProductCatalog;

/** APPLICATION core: implements the driving port, uses only driven ports and the domain. */
public final class OrderService implements OrderingUseCase {
    private final OrderRepository orders;
    private final ProductCatalog catalog;
    private final PaymentGateway payments;
    private final EventPublisher events;
    private final AtomicLong nextId = new AtomicLong(1);

    public OrderService(OrderRepository orders, ProductCatalog catalog, PaymentGateway payments, EventPublisher events) {
        this.orders = orders;
        this.catalog = catalog;
        this.payments = payments;
        this.events = events;
    }

    @Override
    public OrderReceipt place(PlaceOrderCommand cmd) {
        List<OrderLine> lines = cmd.items().stream()
            .map(i -> new OrderLine(i.sku(), i.qty(), catalog.priceOf(i.sku())
                .orElseThrow(() -> new IllegalArgumentException("unknown product " + i.sku()))))
            .toList();
        Order order = Order.place("o-" + nextId.getAndIncrement(), cmd.customerId(), lines);

        boolean paid = payments.charge(order.customerId(), order.total());
        if (paid) order.markPaid(); else order.markPaymentFailed();
        orders.save(order);
        events.publish(paid
            ? new OrderEvent.OrderPaid(order.id(), order.customerId(), order.total())
            : new OrderEvent.PaymentFailed(order.id(), order.customerId(), order.total()));
        return receiptOf(order);
    }

    @Override
    public Optional<OrderReceipt> find(String orderId) {
        return orders.findById(orderId).map(OrderService::receiptOf);
    }

    private static OrderReceipt receiptOf(Order o) { return new OrderReceipt(o.id(), o.status(), o.total()); }
}
