package ch09.jdbc;

/** Value object embedded into the purchase_order row (columns ship_street, ship_city). */
public record Address(String street, String city) {}
