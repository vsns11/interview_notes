package lld.ch07.worked.step5;

import java.util.Objects;

/** Move Function: the shipment now answers its own charge and points (cures Feature Envy). */
public record Shipment(ServiceType type, double weightKg, int distanceKm) {
    public Shipment {
        Objects.requireNonNull(type, "type");
    }

    public double charge() { return type.tariff().charge(weightKg, distanceKm); }

    public int loyaltyPoints() { return type.tariff().loyaltyPoints(distanceKm); }
}
