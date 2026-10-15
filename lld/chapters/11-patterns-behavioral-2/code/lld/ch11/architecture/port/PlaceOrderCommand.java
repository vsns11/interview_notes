package lld.ch11.architecture.port;

import java.util.List;

/** Input to the driving port. Plain data: no HTTP, CSV or JSON types leak in. */
public record PlaceOrderCommand(String customerId, List<Item> items) {
    public record Item(String sku, int qty) {}

    public PlaceOrderCommand { items = List.copyOf(items); }
}
