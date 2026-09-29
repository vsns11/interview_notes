package ch09.cache;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.benmanes.caffeine.cache.stats.CacheStats;
import java.math.BigDecimal;
import java.time.Duration;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.cache.CacheManager;
import org.springframework.cache.caffeine.CaffeineCache;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

@SpringBootTest
class CachingTests {

    @TestConfiguration
    static class ClockConfig {
        @Bean @Primary FakeTicker fakeTicker() { return new FakeTicker(); }   // replaces the system ticker
    }

    @Autowired ProductService products;
    @Autowired CatalogService catalog;
    @Autowired ProductBackend backend;
    @Autowired CacheManager cacheManager;
    @Autowired FakeTicker ticker;

    @BeforeEach
    void emptyCaches() {
        cacheManager.getCacheNames().forEach(n -> cacheManager.getCache(n).clear());
        backend.loads.set(0);
    }

    com.github.benmanes.caffeine.cache.Cache<Object, Object> nativeCache(String name) {
        return ((CaffeineCache) cacheManager.getCache(name)).getNativeCache();
    }

    @Test
    void cacheableRunsTheMethodOnlyOnAMiss() {
        CacheStats before = nativeCache("products").stats();
        for (int i = 1; i <= 3; i++) {
            Product p = products.find("BOOK-1");
            System.out.println("call " + i + ": " + p.name() + "   backend loads so far = " + backend.loads.get());
        }
        CacheStats delta = nativeCache("products").stats().minus(before);
        System.out.println("cache manager: " + cacheManager.getClass().getSimpleName()
                + "   hits=" + delta.hitCount() + " misses=" + delta.missCount());
        assertThat(backend.loads).hasValue(1);
        assertThat(delta.hitCount()).isEqualTo(2);
        assertThat(delta.missCount()).isEqualTo(1);
    }

    @Test
    void cachePutRefreshesAndCacheEvictRemoves() {
        products.find("PEN-7");                                           // miss -> load 1
        products.update(new Product("PEN-7", "Gel pen", new BigDecimal("3.00")));   // method runs + cache put
        System.out.println("after @CachePut:   " + products.find("PEN-7").price() + "  loads=" + backend.loads.get());

        products.delete("PEN-7");                                         // method runs + key evicted
        System.out.println("after @CacheEvict: " + products.find("PEN-7") + "  loads=" + backend.loads.get());
        products.find("PEN-7");                                           // null was not cached (unless)
        System.out.println("asked again:       loads=" + backend.loads.get() + "  (null results are not cached)");

        products.find("BOOK-1");
        products.reloadAll();                                             // allEntries = true
        System.out.println("after reloadAll(): entries in 'products' = " + nativeCache("products").estimatedSize());

        assertThat(backend.loads).hasValue(4);
        assertThat(nativeCache("products").estimatedSize()).isZero();
    }

    @Test
    void entriesExpireAfterTheTtl() {
        products.find("BOOK-1");                                          // load 1, written at t=0
        ticker.advance(Duration.ofMinutes(9));
        products.find("BOOK-1");
        System.out.println("t=9 min:  loads=" + backend.loads.get() + "  (still fresh -> hit)");
        ticker.advance(Duration.ofMinutes(2));
        products.find("BOOK-1");
        System.out.println("t=11 min: loads=" + backend.loads.get() + "  (expired after 10 min -> miss -> reload)");
        assertThat(backend.loads).hasValue(2);
    }

    @Test
    void defaultKeysAndTheirCollisions() {
        System.out.println("name(\"A\")             -> " + catalog.name("A"));
        System.out.println("description(\"A\")      -> " + catalog.description("A") + "   <- WRONG: same cache, same key");
        System.out.println("descriptionFixed(\"A\") -> " + catalog.descriptionFixed("A"));
        catalog.price("A", "EUR");
        catalog.priceEurOnly("B", "USD");                                 // condition false -> not cached
        catalog.priceEurOnly("C", "EUR");                                 // condition true  -> cached under "C"
        nativeCache("catalog").asMap().keySet().stream().map(String::valueOf).sorted()
                .forEach(k -> System.out.println("key in 'catalog': " + k));
        assertThat(catalog.description("A")).isEqualTo("name of A");
        assertThat(nativeCache("catalog").asMap()).doesNotContainKey("B");
    }

    @Test
    void cachedMutableObjectsCanBeChangedByAnyCaller() {
        List<String> first = products.tags("BOOK-1");
        first.add("hacked");                                              // caller changes "its" list...
        List<String> second = products.tags("BOOK-1");                    // ...another caller gets the same object
        System.out.println("second caller sees: " + second + "   same instance: " + (first == second));
        assertThat(second).contains("hacked");
    }
}
