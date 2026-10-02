package com.example.demo;

import com.example.greeting.core.GreetingService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionEvaluationReport;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ConfigurableApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class DemoApplicationTests {

    @Autowired GreetingService greetings;          // provided by the starter's auto-configuration
    @Autowired ConfigurableApplicationContext ctx;

    @Test
    void starterAutoConfiguresTheService() {
        var outcome = ConditionEvaluationReport.get(ctx.getBeanFactory()).getConditionAndOutcomesBySource()
                .get("com.example.greeting.autoconfigure.GreetingAutoConfiguration");
        System.out.println("app says: " + greetings.greet("Ada"));
        System.out.println("GreetingAutoConfiguration matched? " + outcome.isFullMatch());
        assertThat(greetings.greet("Ada")).isEqualTo("Good morning, Ada!");
    }
}
