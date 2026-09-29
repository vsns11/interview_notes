package lld.ch20.ratelimiter;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

enum Tier { FREE, PREMIUM }

enum Algorithm { TOKEN_BUCKET, FIXED_WINDOW, SLIDING_LOG, SLIDING_COUNTER }

/** Configuration: which algorithm and how many requests per window (token bucket: burst = limit). */
record LimitRule(Algorithm algorithm, long limit, Duration window) {}

/** Factory: turns a rule into a fresh Limiter object. The only place that knows the concrete classes. */
final class LimiterFactory {
    private LimiterFactory() {}

    static Limiter create(List<LimitRule> rules, Ticker ticker) {
        if (rules.size() == 1) return create(rules.get(0), ticker);
        return new CompositeLimiter(rules.stream().map(r -> create(r, ticker)).toList());
    }

    static Limiter create(LimitRule r, Ticker ticker) {
        return switch (r.algorithm()) {
            case TOKEN_BUCKET -> new TokenBucket(r.limit(), r.limit() / (r.window().toNanos() / 1e9), ticker);
            case FIXED_WINDOW -> new FixedWindow(r.limit(), r.window(), ticker);
            case SLIDING_LOG -> new SlidingWindowLog(r.limit(), r.window(), ticker);
            case SLIDING_COUNTER -> new SlidingWindowCounter(r.limit(), r.window(), ticker);
        };
    }
}

/** Facade used by an API gateway / servlet filter: "may user U call API A right now?" */
final class RateLimiterService {
    private record Key(String userId, String api) {}

    private final Map<String, Map<Tier, List<LimitRule>>> rules = new ConcurrentHashMap<>();
    private final List<LimitRule> defaultRules;
    private final Map<Key, Limiter> limiters = new ConcurrentHashMap<>();
    private final Map<String, Tier> tiers = new ConcurrentHashMap<>();
    private final Ticker ticker;

    RateLimiterService(List<LimitRule> defaultRules, Ticker ticker) {
        this.defaultRules = List.copyOf(defaultRules);
        this.ticker = ticker;
    }

    void addRule(String api, Tier tier, LimitRule... r) {
        rules.computeIfAbsent(api, a -> new ConcurrentHashMap<>()).put(tier, List.of(r));
    }

    void setTier(String userId, Tier tier) { tiers.put(userId, tier); }

    Decision check(String userId, String api) {
        // computeIfAbsent creates exactly ONE limiter per (user, api), even under a race
        Limiter limiter = limiters.computeIfAbsent(new Key(userId, api), k -> {
            Tier tier = tiers.getOrDefault(userId, Tier.FREE);
            List<LimitRule> r = rules.getOrDefault(api, Map.of()).getOrDefault(tier, defaultRules);
            return LimiterFactory.create(r, ticker);
        });
        return limiter.tryAcquire();
    }
}
