package ch04.features;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

@Service
public class PriceLookup {

    private final AtomicInteger realCalls = new AtomicInteger();

    @Cacheable("prices")                    // proxy checks the "prices" cache before calling us
    public int price(String sku) {
        realCalls.incrementAndGet();         // counts how often the real method body runs
        return sku.length() * 100;
    }

    public int priceTwiceViaThis(String sku) {
        return this.price(sku) + this.price(sku);   // self-invocation: cache never consulted
    }

    public int realCalls() { return realCalls.get(); }
}
