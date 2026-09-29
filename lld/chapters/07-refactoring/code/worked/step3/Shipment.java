package lld.ch07.worked.step3;

/** Encapsulate Record: the public mutable class became an immutable record. */
public record Shipment(String type, double weightKg, int distanceKm) { }
