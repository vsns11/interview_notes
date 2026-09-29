package com.example.ioc;

import com.example.ioc.lazy.RateCache;
import com.example.ioc.lazy.ReportGenerator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class LazyInitTest {

    @Test
    void lazyBeanIsCreatedOnFirstUse() {
        Trace.clear();
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.lazy")) {
            Trace.add("-- context started");
            ctx.getBean(ReportGenerator.class);
        }
        Trace.print("@Lazy");
        assertThat(Trace.lines()).containsExactly(
                "RateCache created (eager)", "-- context started", "ReportGenerator created (lazy)");
    }

    @Configuration @ComponentScan("com.example.ioc.lazy")
    static class LazyApp {}

    @Test
    void springBootCanMakeEveryBeanLazy() {
        Trace.clear();
        try (var ctx = new SpringApplicationBuilder(LazyApp.class).web(WebApplicationType.NONE)
                .properties("spring.main.lazy-initialization=true", "spring.main.banner-mode=off",
                        "logging.level.root=WARN").run()) {
            Trace.add("-- context started");
            ctx.getBean(RateCache.class);
        }
        Trace.print("spring.main.lazy-initialization=true");
        assertThat(Trace.lines()).containsExactly("-- context started", "RateCache created (eager)");
    }
}
