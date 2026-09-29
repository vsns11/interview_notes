package ch15.obs;

import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import io.micrometer.observation.annotation.Observed;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class OrderService {

    private static final Logger log = LoggerFactory.getLogger(OrderService.class);

    private final Map<Long, String> orders = new ConcurrentHashMap<>(Map.of(1L, "keyboard", 2L, "mouse"));
    private final MeterRegistry registry;
    private final Timer placeTimer;

    public OrderService(MeterRegistry registry) {
        this.registry = registry;
        // A Timer records how many calls and how long they took
        this.placeTimer = Timer.builder("orders.place")
                .description("time to place an order")
                .register(registry);
        // A Gauge samples a current value on demand (here: the map size)
        registry.gauge("orders.stored", orders, Map::size);
    }

    // @Observed = one Observation -> a timer metric "order.lookup" AND a span "order-lookup"
    @Observed(name = "order.lookup", contextualName = "order-lookup")
    public Optional<String> find(long id) {
        log.info("looking up order {}", id);
        Optional<String> item = Optional.ofNullable(orders.get(id));
        // A Counter only goes up; the tag splits it into one time series per result
        Counter.builder("orders.lookups")
                .tag("result", item.isPresent() ? "found" : "missing")
                .register(registry)
                .increment();
        return item;
    }

    public long place(String item) {
        return placeTimer.record(() -> {
            long id = orders.size() + 1L;
            orders.put(id, item);
            log.info("placed order {} for {}", id, item);
            return id;
        });
    }
}
