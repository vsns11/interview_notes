package ch09.cache;

import com.github.benmanes.caffeine.cache.Caffeine;
import com.github.benmanes.caffeine.cache.Ticker;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class CacheConfig {

    /** The clock Caffeine uses for expiry. Production: Ticker.systemTicker(). Tests replace it. */
    @Bean
    Ticker cacheTicker() {
        return Ticker.systemTicker();
    }

    /** Boot's CaffeineCacheManager uses this builder for every cache it creates. */
    @Bean
    Caffeine<Object, Object> caffeine(Ticker cacheTicker) {
        return Caffeine.newBuilder()
                .maximumSize(10_000)                        // bound memory: evicts least-useful entries
                .expireAfterWrite(Duration.ofMinutes(10))   // TTL: entries are at most 10 minutes old
                .ticker(cacheTicker)
                .recordStats();                             // hit/miss counters (also used by Actuator metrics)
    }
}
