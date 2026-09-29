package com.example.scopes;

import java.util.concurrent.atomic.AtomicInteger;

import org.springframework.stereotype.Component;
import org.springframework.web.context.annotation.RequestScope;

@Component
@RequestScope          // one instance per HTTP request; a scoped proxy is injected into singletons
public class RequestInfo {
    private static final AtomicInteger SEQ = new AtomicInteger();
    private final int id = SEQ.incrementAndGet();
    public int id() { return id; }
}
