package com.example.ioc;

import java.util.Arrays;
import java.util.Map;

import com.example.ioc.nullsafety.ReportService;
import com.example.ioc.registrar.ChannelRegistrar.Router;
import com.example.ioc.registrar.RegistrarConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.MapPropertySource;

import static org.assertj.core.api.Assertions.assertThat;

class NullSafetyAndRegistrarTest {

    @Test
    void nullableParameterMeansOptionalDependency() {
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.nullsafety")) {
            ReportService s = ctx.getBean(ReportService.class);
            System.out.println("no Translator bean -> title = " + s.title("Q3 sales")
                    + ", findTitle(2) = " + s.findTitle(2));
            assertThat(s.title("Q3 sales")).isEqualTo("Q3 sales");
        }
    }

    @Test
    void beanRegistrarRegistersBeansProgrammatically() {
        var ctx = new AnnotationConfigApplicationContext();
        ctx.getEnvironment().getPropertySources()
                .addFirst(new MapPropertySource("t", Map.of("channels", "email,sms,push")));
        ctx.register(RegistrarConfig.class);
        ctx.refresh();
        try (ctx) {
            String[] names = Arrays.stream(ctx.getBeanDefinitionNames())
                    .filter(n -> n.endsWith("Channel") || n.equals("router")).toArray(String[]::new);
            System.out.println("registered: " + String.join(", ", names));
            System.out.println("router -> " + ctx.getBean(Router.class));
            assertThat(names).containsExactly("emailChannel", "smsChannel", "pushChannel", "router");
        }
    }
}
