package com.example.ioc;

import java.util.Arrays;

import com.example.ioc.definitions.AuditLog;
import com.example.ioc.definitions.FullConfig;
import com.example.ioc.definitions.LiteConfig;
import com.example.ioc.definitions.LiteSafeConfig;
import com.example.ioc.definitions.Services.OrderService;
import com.example.ioc.definitions.Services.PaymentService;
import com.example.ioc.definitions.scan.InvoiceService;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class BeanDefinitionsTest {

    @Test
    void componentScanning_findsStereotypes() {
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.definitions.scan")) {
            String[] mine = Arrays.stream(ctx.getBeanDefinitionNames())
                    .filter(n -> !n.startsWith("org.springframework")).sorted().toArray(String[]::new);
            System.out.println("scanned beans: " + String.join(", ", mine));
            assertThat(mine).containsExactly("invoiceRepository", "invoiceService", "renderer");
            assertThat(ctx.getBean(InvoiceService.class).repository()).isNotNull();
        }
    }

    @Test
    void fullConfiguration_interBeanCallsReturnTheSingleton() {
        AuditLog.CREATED.set(0);
        try (var ctx = new AnnotationConfigApplicationContext(FullConfig.class)) {
            int a = ctx.getBean(OrderService.class).log().id, b = ctx.getBean(PaymentService.class).log().id;
            System.out.printf("full: config class = %s%n", ctx.getBean(FullConfig.class).getClass().getSimpleName());
            System.out.printf("full: order.log=#%d payment.log=#%d, AuditLogs created=%d%n", a, b, AuditLog.CREATED.get());
            assertThat(a).isEqualTo(b);
            assertThat(AuditLog.CREATED.get()).isEqualTo(1);
        }
    }

    @Test
    void liteConfiguration_interBeanCallsAreJustJava() {
        AuditLog.CREATED.set(0);
        try (var ctx = new AnnotationConfigApplicationContext(LiteConfig.class)) {
            int a = ctx.getBean(OrderService.class).log().id, b = ctx.getBean(PaymentService.class).log().id;
            System.out.printf("lite: config class = %s%n", ctx.getBean(LiteConfig.class).getClass().getSimpleName());
            System.out.printf("lite: order.log=#%d payment.log=#%d, AuditLogs created=%d%n", a, b, AuditLog.CREATED.get());
            assertThat(a).isNotEqualTo(b);
            assertThat(AuditLog.CREATED.get()).isEqualTo(3);   // the bean + two stray copies
        }
    }

    @Test
    void liteConfiguration_withParameters_isSafe() {
        AuditLog.CREATED.set(0);
        try (var ctx = new AnnotationConfigApplicationContext(LiteSafeConfig.class)) {
            int a = ctx.getBean(OrderService.class).log().id, b = ctx.getBean(PaymentService.class).log().id;
            System.out.printf("lite+params: order.log=#%d payment.log=#%d, AuditLogs created=%d%n", a, b, AuditLog.CREATED.get());
            assertThat(a).isEqualTo(b);
        }
    }
}
