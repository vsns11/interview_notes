package lld.ch09.proxy;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;

/** CACHING PROXY: bounded (LRU) and time-limited, because an unbounded cache is a leak. */
final class CachingRateProxy implements RateService {
    private record CachedRate(BigDecimal value, long expiresAt) { }

    private final RateService inner;
    private final long ttlMillis;
    private final Map<String, CachedRate> cache;

    CachingRateProxy(RateService inner, int maxEntries, long ttlMillis) {
        this.inner = inner;
        this.ttlMillis = ttlMillis;
        this.cache = new LinkedHashMap<>(16, 0.75f, true) {   // access order = LRU
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, CachedRate> eldest) {
                return size() > maxEntries;
            }
        };
    }

    @Override
    public synchronized BigDecimal rate(String from, String to) {
        String key = from + "->" + to;
        CachedRate e = cache.get(key);
        long now = System.currentTimeMillis();
        if (e != null && e.expiresAt() > now) return e.value();
        BigDecimal v = inner.rate(from, to);
        cache.put(key, new CachedRate(v, now + ttlMillis));
        return v;
    }

    @Override
    public BigDecimal convert(BigDecimal amount, String from, String to) {
        return RateService.toMoney(amount.multiply(rate(from, to)));
    }
}
