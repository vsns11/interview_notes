package com.example.ioc;

import java.util.Map;

import com.example.ioc.conditional.PaymentBeans;
import com.example.ioc.conditional.PaymentBeans.FraudChecker;
import com.example.ioc.conditional.PaymentBeans.PaymentGateway;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class ConditionalAndProfilesTest {

    static AnnotationConfigApplicationContext start(String profile, Map<String, Object> props) {
        var ctx = new AnnotationConfigApplicationContext();
        if (profile != null) ctx.getEnvironment().setActiveProfiles(profile);
        ctx.getEnvironment().getPropertySources().addFirst(new MapPropertySource("test", props));
        ctx.register(PaymentBeans.class);
        ctx.refresh();
        return ctx;
    }

    @Test
    void profilesAndConditionsDecideWhichBeansExist() {
        try (var dev = start("dev", Map.of("features.fraud-check", "true"));
             var prod = start(null, Map.of())) {
            System.out.println("profile dev : " + dev.getBean(PaymentGateway.class).name()
                    + ", fraudChecker beans = " + dev.getBeanNamesForType(FraudChecker.class).length);
            System.out.println("no profile  : " + prod.getBean(PaymentGateway.class).name()
                    + ", fraudChecker beans = " + prod.getBeanNamesForType(FraudChecker.class).length);
            assertThat(dev.getBean(PaymentGateway.class).name()).startsWith("fake");
            assertThat(prod.getBean(PaymentGateway.class).name()).startsWith("HTTP");
            assertThat(dev.containsBean("fraudChecker")).isTrue();
            assertThat(prod.containsBean("fraudChecker")).isFalse();
        }
    }
}
