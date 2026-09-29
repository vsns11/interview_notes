package lld.ch07.worked.step5;

import java.util.List;

public record Invoice(String customer, List<Shipment> shipments) {
    public Invoice {
        shipments = List.copyOf(shipments);
    }
}
