package lld.ch01.parkinglot;

/** A vehicle is pure data: its plate and its type. */
public record Vehicle(String plate, VehicleType type) { }
