package com.example.greeting.autoconfigure;

import com.example.greeting.core.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.FilteredClassLoader;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import static org.assertj.core.api.Assertions.assertThat;

class GreetingAutoConfigurationTest {

    // A tiny, fast context per test: no main class, no component scan, no files.
    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(GreetingAutoConfiguration.class));

    @Test
    void createsTheBeanByDefault() {
        runner.run(ctx -> {
            System.out.println("default         -> " + ctx.getBean(GreetingService.class).greet("Ada"));
            assertThat(ctx).hasSingleBean(GreetingService.class);
        });
    }

    @Test
    void propertiesCustomiseIt() {
        runner.withPropertyValues("greeting.template=Welcome back, %s.").run(ctx -> {
            System.out.println("with property   -> " + ctx.getBean(GreetingService.class).greet("Ada"));
            assertThat(ctx.getBean(GreetingService.class).greet("Ada")).isEqualTo("Welcome back, Ada.");
        });
    }

    @Test
    void canBeSwitchedOff() {
        runner.withPropertyValues("greeting.enabled=false").run(ctx -> {
            System.out.println("enabled=false   -> GreetingService beans: " + ctx.getBeansOfType(GreetingService.class).size());
            assertThat(ctx).doesNotHaveBean(GreetingService.class);
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class UserConfig {
        @Bean GreetingService myGreeting() { return new GreetingService("Yo %s"); }
    }

    @Test
    void backsOffWhenTheUserDefinesOne() {
        runner.withUserConfiguration(UserConfig.class).run(ctx -> {
            System.out.println("user bean       -> " + ctx.getBean(GreetingService.class).greet("Ada")
                    + " (bean name: " + ctx.getBeanNamesForType(GreetingService.class)[0] + ")");
            assertThat(ctx).hasSingleBean(GreetingService.class).hasBean("myGreeting");
        });
    }

    @Test
    void skippedWhenLibraryIsMissing() {
        runner.withClassLoader(new FilteredClassLoader(GreetingService.class)).run(ctx -> {
            System.out.println("class missing   -> has greetingService? " + ctx.containsBean("greetingService"));
            assertThat(ctx).doesNotHaveBean("greetingService");
        });
    }
}
