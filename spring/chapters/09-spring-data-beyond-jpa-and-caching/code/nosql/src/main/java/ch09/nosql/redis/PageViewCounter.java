package ch09.nosql.redis;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

/** Most Redis use is not repositories but plain commands through a template. */
@Component
public class PageViewCounter {
    private final StringRedisTemplate redis;

    public PageViewCounter(StringRedisTemplate redis) { this.redis = redis; }

    public long hit(String page) {
        return redis.opsForValue().increment("views:" + page);   // INCR is atomic on the Redis server
    }
}
