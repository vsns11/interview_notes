package ch09.cache;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.stereotype.Component;

/** Stands in for a slow database or remote service. Counts how often it is really called. */
@Component
public class ProductBackend {
    public final AtomicInteger loads = new AtomicInteger();
    private final Map<String, Product> rows = new ConcurrentHashMap<>(Map.of(
            "BOOK-1", new Product("BOOK-1", "Spring in Depth", new BigDecimal("40.00")),
            "PEN-7", new Product("PEN-7", "Gel pen", new BigDecimal("2.50"))));

    public Product load(String sku) {
        loads.incrementAndGet();
        return rows.get(sku);
    }

    public Product store(Product p) {
        rows.put(p.sku(), p);
        return p;
    }

    public void remove(String sku) { rows.remove(sku); }

    public List<String> tags(String sku) {
        loads.incrementAndGet();
        return List.of("new", "sale");
    }
}
