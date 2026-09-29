package lld.ch01.parkinglot;

/** Records which vehicle went into which spot, and when. The "connection object" between Vehicle and Spot. */
public record Ticket(String id, Vehicle vehicle, ParkingSpot spot, long entryMinute) { }
