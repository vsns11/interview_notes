package lld.ch07.worked.step3;

import java.util.List;

/** Encapsulate Collection: callers get an unmodifiable copy, never the live list. */
public record Invoice(String customer, List<Shipment> shipments) {
    public Invoice {
        shipments = List.copyOf(shipments);
    }
}
