package ch09.nosql.redis;

import java.util.List;
import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.index.Indexed;

/** Stored as a Redis hash under key "carts:<id>"; removed by Redis after 1 hour (timeToLive, seconds). */
@RedisHash(value = "carts", timeToLive = 3600)
public record Cart(@Id String id, @Indexed String userId, List<CartItem> items) {

    public record CartItem(String sku, int quantity) {}
}
