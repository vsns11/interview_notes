package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.core.annotation.Order;

/** Sheet 19.3 / 19.7: how Spring picks among several beans of one type. */
class BeanSelectionTests {

    interface Notifier { String name(); }

    record Consumer(Notifier byType, Notifier qualified, Notifier namedPush, List<Notifier> all, Map<String, Notifier> byName) { }

    @Configuration(proxyBeanMethods = false)
    static class Config {
        @Bean @Primary @Order(2) Notifier sms()   { return () -> "sms"; }
        @Bean @Order(1)          Notifier email() { return () -> "email"; }
        @Bean @Order(3)          Notifier push()  { return () -> "push"; }

        @Bean Consumer consumer(Notifier byType,                          // several candidates -> @Primary
                                @Qualifier("email") Notifier qualified,   // qualifier beats @Primary
                                Notifier push,                            // name match loses to @Primary
                                List<Notifier> all,                       // all, sorted by @Order
                                Map<String, Notifier> byName) {           // bean name -> bean
            return new Consumer(byType, qualified, push, all, byName);
        }
    }

    @Test
    void qualifierThenPrimaryThenName() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            Consumer c = ctx.getBean(Consumer.class);
            System.out.println("by type (@Primary)        -> " + c.byType().name());
            System.out.println("@Qualifier(\"email\")        -> " + c.qualified().name());
            System.out.println("parameter named 'push'    -> " + c.namedPush().name() + "  (@Primary wins over the name)");
            System.out.println("List<Notifier> by @Order  -> " + c.all().stream().map(Notifier::name).toList());
            System.out.println("Map keys (bean names)     -> " + c.byName().keySet().stream().sorted().toList());
            assertThat(c.byType().name()).isEqualTo("sms");
            assertThat(c.qualified().name()).isEqualTo("email");
            assertThat(c.namedPush().name()).isEqualTo("sms");
            assertThat(c.all()).extracting(Notifier::name).containsExactly("email", "sms", "push");
            assertThat(c.byName()).containsOnlyKeys("sms", "email", "push");
        }
    }
}
