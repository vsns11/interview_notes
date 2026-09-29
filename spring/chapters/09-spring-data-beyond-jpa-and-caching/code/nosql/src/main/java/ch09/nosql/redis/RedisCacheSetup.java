package ch09.nosql.redis;

import java.time.Duration;
import org.springframework.boot.cache.autoconfigure.RedisCacheManagerBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.serializer.GenericJacksonJsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext.SerializationPair;
import tools.jackson.databind.jsontype.BasicPolymorphicTypeValidator;

@Configuration(proxyBeanMethods = false)
public class RedisCacheSetup {

    /**
     * Defaults for every Redis cache: JSON values (readable, language-neutral) instead of JDK
     * serialization. The JSON stores each value's class name so it can be read back; the validator
     * allows only our own and JDK types to be created from that name (never "any class").
     */
    @Bean
    RedisCacheConfiguration redisCacheConfiguration() {
        var allowedTypes = BasicPolymorphicTypeValidator.builder()
                .allowIfSubType("ch09.")
                .allowIfSubType("java.")
                .build();
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(Duration.ofMinutes(10))
                .disableCachingNullValues()
                .serializeValuesWith(SerializationPair.fromSerializer(
                        GenericJacksonJsonRedisSerializer.builder().enableDefaultTyping(allowedTypes).build()));
    }

    /** Per-cache overrides on top of the defaults. */
    @Bean
    RedisCacheManagerBuilderCustomizer perCacheTtl(RedisCacheConfiguration defaults) {
        return builder -> builder
                .withCacheConfiguration("products", defaults.entryTtl(Duration.ofMinutes(30)))
                .withCacheConfiguration("prices", defaults.entryTtl(Duration.ofSeconds(30)));
    }
}
