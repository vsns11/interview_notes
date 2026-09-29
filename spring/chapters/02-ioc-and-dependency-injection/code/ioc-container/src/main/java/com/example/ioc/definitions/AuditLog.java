package com.example.ioc.definitions;

import java.util.concurrent.atomic.AtomicInteger;

public class AuditLog {
    public static final AtomicInteger CREATED = new AtomicInteger();
    public final int id = CREATED.incrementAndGet();
}
