package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.cache.interceptor.SimpleKey;
import org.springframework.cache.interceptor.SimpleKeyGenerator;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.resilience.annotation.Retryable;
import org.springframework.scheduling.support.CronExpression;

/** Sheets 19.3 and 19.12: cache keys, six-field cron, Framework 7 @Retryable. */
class CoreUtilitiesTests {

    @Test
    void defaultCacheKeys() {
        Object none = SimpleKeyGenerator.generateKey();
        Object one = SimpleKeyGenerator.generateKey("BOOK-1");
        Object two = SimpleKeyGenerator.generateKey("BOOK-1", "EUR");
        System.out.println("SimpleKeyGenerator: () -> " + none + ", (\"BOOK-1\") -> " + one + ", (\"BOOK-1\",\"EUR\") -> " + two);
        assertThat(none).isEqualTo(SimpleKey.EMPTY);
        assertThat(one).isEqualTo("BOOK-1");                    // the argument itself - no method name!
        assertThat(two).isEqualTo(new SimpleKey("BOOK-1", "EUR"));
    }

    @Test
    void cronHasSixFields() {
        LocalDateTime from = LocalDateTime.of(2026, 9, 28, 10, 0);   // a Monday
        LocalDateTime next = CronExpression.parse("0 30 2 * * MON-FRI").next(from);
        System.out.println("'0 30 2 * * MON-FRI' after " + from + " -> " + next);
        assertThat(next).isEqualTo(LocalDateTime.of(2026, 9, 29, 2, 30));
        assertThatThrownBy(() -> CronExpression.parse("0 2 * * *")).isInstanceOf(IllegalArgumentException.class);
        System.out.println("five-field Unix cron '0 2 * * *' -> IllegalArgumentException");
    }

    static class Flaky {
        private final List<String> attempts = new ArrayList<>();
        public List<String> attempts() { return attempts; }   // read state through a method: the bean is a CGLIB proxy
        @Retryable(maxRetries = 2, delay = 5)
        public String call() {
            attempts.add("attempt " + (attempts.size() + 1));
            throw new IllegalStateException("still down");
        }
    }

    @Configuration(proxyBeanMethods = false)
    @EnableResilientMethods                                       // without it @Retryable is ignored
    static class ResilienceConfig {
        @Bean Flaky flaky() { return new Flaky(); }
    }

    @Test
    void retryableDefaultsAndAttempts() throws Exception {
        Object maxRetries = Retryable.class.getMethod("maxRetries").getDefaultValue();
        Object delay = Retryable.class.getMethod("delay").getDefaultValue();
        Object multiplier = Retryable.class.getMethod("multiplier").getDefaultValue();
        System.out.println("@Retryable defaults: maxRetries=" + maxRetries + ", delay=" + delay + " ms, multiplier=" + multiplier);
        assertThat(maxRetries).isEqualTo(3L);
        assertThat(delay).isEqualTo(1000L);

        try (var ctx = new AnnotationConfigApplicationContext(ResilienceConfig.class)) {
            Flaky flaky = ctx.getBean(Flaky.class);
            assertThatThrownBy(flaky::call).isInstanceOf(IllegalStateException.class).hasMessage("still down");
            System.out.println("maxRetries = 2 -> " + flaky.attempts() + ", then the last exception is rethrown");
            assertThat(flaky.attempts()).hasSize(3);                   // 1 call + 2 retries
        }
    }
}
