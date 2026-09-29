package ch14.rabbit;

public record OrderEvent(String orderId, String customer, long cents) {}
