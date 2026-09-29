package com.example.ioc.definitions;

/** Two plain classes that both need the AuditLog. */
public final class Services {
    private Services() {}
    public record OrderService(AuditLog log) {}
    public record PaymentService(AuditLog log) {}
}
