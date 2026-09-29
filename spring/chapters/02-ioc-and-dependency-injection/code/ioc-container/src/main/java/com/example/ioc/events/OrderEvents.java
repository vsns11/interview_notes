package com.example.ioc.events;

/** Events can be any object; records are a good fit (immutable). */
public final class OrderEvents {
    private OrderEvents() {}
    public record OrderPlaced(String orderId, long cents) {}
    public record StockReserved(String orderId) {}
}
