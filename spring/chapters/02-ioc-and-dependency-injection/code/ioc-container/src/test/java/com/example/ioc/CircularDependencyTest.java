package com.example.ioc;

import com.example.ioc.circular.field.FieldCycle;
import com.example.ioc.circular.lazy.LazyCycle;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.BeanCurrentlyInCreationException;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.NestedExceptionUtils;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CircularDependencyTest {

    @Test
    void constructorCycle_isImpossible() {
        assertThatThrownBy(() -> new AnnotationConfigApplicationContext("com.example.ioc.circular.ctor"))
                .satisfies(e -> {
                    Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
                    System.out.println("constructor cycle -> " + root.getClass().getSimpleName() + ": "
                            + root.getMessage().replaceAll("\\s+", " ").substring(0, 95) + "...");
                    assertThat(root).isInstanceOf(BeanCurrentlyInCreationException.class);
                });
    }

    @Test
    void fieldCycle_worksInPlainSpring() {
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.circular.field")) {
            var orders = ctx.getBean(FieldCycle.OrderService.class);
            System.out.println("field cycle, plain Spring -> OK, orders.customers.orders == orders? "
                    + (orders.customers.orders == orders));
            assertThat(orders.customers.orders).isSameAs(orders);
        }
    }

    @Configuration @ComponentScan("com.example.ioc.circular.field")
    static class FieldCycleApp {}

    @Test
    void fieldCycle_isRejectedBySpringBootByDefault() {
        assertThatThrownBy(() -> new SpringApplicationBuilder(FieldCycleApp.class)
                        .web(WebApplicationType.NONE).logStartupInfo(false)
                        .properties("spring.main.banner-mode=off", "logging.level.root=OFF").run())
                .satisfies(e -> System.out.println("field cycle, Spring Boot -> "
                        + NestedExceptionUtils.getMostSpecificCause(e).getClass().getSimpleName()))
                .rootCause().isInstanceOf(BeanCurrentlyInCreationException.class);
    }

    @Test
    void lazyBreaksTheCycle() {
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.circular.lazy")) {
            var orders = ctx.getBean(LazyCycle.OrderService.class);
            System.out.println("@Lazy cycle -> OK, injected class = "
                    + orders.customers.getClass().getSimpleName() + ", call = " + orders.customers.name());
            assertThat(orders.customers.name()).isEqualTo("customers");
        }
    }
}
