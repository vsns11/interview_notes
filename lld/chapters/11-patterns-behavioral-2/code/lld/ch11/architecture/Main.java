package lld.ch11.architecture;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import lld.ch11.architecture.adapter.CsvOrderImporter;
import lld.ch11.architecture.adapter.InMemoryCatalog;
import lld.ch11.architecture.adapter.InMemoryEventBus;
import lld.ch11.architecture.adapter.InMemoryOrderRepository;
import lld.ch11.architecture.adapter.LimitPaymentGateway;
import lld.ch11.architecture.app.OrderService;
import lld.ch11.architecture.domain.OrderEvent;
import lld.ch11.architecture.port.OrderingUseCase;
import lld.ch11.architecture.port.PlaceOrderCommand;

/** COMPOSITION ROOT: the only class that knows every adapter. It wires them to the core. */
public class Main {
    public static void main(String[] args) {
        var catalog = new InMemoryCatalog(Map.of(
            "book", new BigDecimal("12.99"), "pen", new BigDecimal("0.99"), "lamp", new BigDecimal("89.00")));
        var bus = new InMemoryEventBus();
        bus.subscribe(OrderEvent.OrderPaid.class, e -> System.out.println("    [warehouse] reserve stock for " + e.orderId()));
        bus.subscribe(OrderEvent.OrderPaid.class, e -> System.out.println("    [email] to " + e.customerId() + ": order " + e.orderId() + " confirmed"));
        bus.subscribe(OrderEvent.class, e -> System.out.println("    [audit] " + e));

        OrderingUseCase ordering = new OrderService(
            new InMemoryOrderRepository(), catalog, new LimitPaymentGateway(new BigDecimal("100.00")), bus);

        System.out.println("== 1. Driving adapter: CSV -> pipe-and-filter -> port -> core -> events ==");
        new CsvOrderImporter(ordering).importLines(List.of(
            "# customer, sku:qty, ...",
            "  C-1, Book:2,  pen:3",
            "",
            "c-2, lamp:2",
            "c-3, pen:zero",
            "c-4, kettle:1",
            "c-5"));

        System.out.println("== 2. Another driving adapter (a test) calls the same port ==");
        System.out.println("  find(o-1) = " + ordering.find("o-1"));
        System.out.println("  find(o-9) = " + ordering.find("o-9"));

        System.out.println("== 3. Swap a driven adapter: same core, a lambda that declines every payment ==");
        OrderingUseCase declining = new OrderService(new InMemoryOrderRepository(), catalog, (customer, amount) -> false, bus);
        System.out.println("  " + declining.place(new PlaceOrderCommand("c-9", List.of(new PlaceOrderCommand.Item("pen", 1)))));
    }
}
