package com.example.ioc.scopes;

import java.util.concurrent.atomic.AtomicInteger;

import com.example.ioc.Trace;
import jakarta.annotation.PreDestroy;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(ConfigurableBeanFactory.SCOPE_PROTOTYPE)     // a NEW instance every time one is requested
public class ShoppingCart {
    private static final AtomicInteger SEQ = new AtomicInteger();
    public final int id = SEQ.incrementAndGet();

    @PreDestroy void close() { Trace.add("ShoppingCart#" + id + " @PreDestroy"); } // never called for prototypes
}
