package com.example.ioc.scopes;

import java.util.concurrent.atomic.AtomicInteger;

import com.example.ioc.Trace;
import jakarta.annotation.PreDestroy;
import org.springframework.stereotype.Component;

@Component                                          // singleton is the default scope
public class PriceList {
    private static final AtomicInteger SEQ = new AtomicInteger();
    public final int id = SEQ.incrementAndGet();

    @PreDestroy void close() { Trace.add("PriceList#" + id + " @PreDestroy"); }
}
