package ch18.proofs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.NoUniqueBeanDefinitionException;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

/** Q: "Two beans of the same type — which one gets injected: @Primary or @Qualifier?" */
class BeanResolutionTests {

    interface PaymentGateway { String name(); }
    record Gateway(String name) implements PaymentGateway { }

    record Checkout(PaymentGateway gateway) { }
    record Refunds(PaymentGateway gateway) { }

    @Configuration(proxyBeanMethods = false)
    static class TwoGateways {
        @Bean @Primary PaymentGateway stripe() { return new Gateway("stripe"); }
        @Bean PaymentGateway paypal() { return new Gateway("paypal"); }
        @Bean Checkout checkout(PaymentGateway gateway) { return new Checkout(gateway); }
        @Bean Refunds refunds(@Qualifier("paypal") PaymentGateway gateway) { return new Refunds(gateway); }
    }

    @Test
    void qualifierBeatsPrimary() {
        new ApplicationContextRunner().withUserConfiguration(TwoGateways.class).run(ctx -> {
            String plain = ctx.getBean(Checkout.class).gateway().name();
            String qualified = ctx.getBean(Refunds.class).gateway().name();
            System.out.println("no qualifier       -> " + plain + "  (the @Primary bean)");
            System.out.println("@Qualifier(paypal) -> " + qualified + "  (qualifier wins over @Primary)");
            assertThat(plain).isEqualTo("stripe");
            assertThat(qualified).isEqualTo("paypal");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class NoPrimaryParameterName {
        @Bean PaymentGateway stripe() { return new Gateway("stripe"); }
        @Bean PaymentGateway paypal() { return new Gateway("paypal"); }
        // parameter name "paypal" matches a bean name -> used as a last-resort tie-breaker
        @Bean Checkout checkout(PaymentGateway paypal) { return new Checkout(paypal); }
    }

    @Test
    void parameterNameIsTheLastTieBreaker() {
        new ApplicationContextRunner().withUserConfiguration(NoPrimaryParameterName.class).run(ctx -> {
            String chosen = ctx.getBean(Checkout.class).gateway().name();
            System.out.println("no @Primary, parameter named 'paypal' -> " + chosen);
            assertThat(chosen).isEqualTo("paypal");
        });
    }

    @Configuration(proxyBeanMethods = false)
    static class Ambiguous {
        @Bean PaymentGateway stripe() { return new Gateway("stripe"); }
        @Bean PaymentGateway paypal() { return new Gateway("paypal"); }
        @Bean Checkout checkout(PaymentGateway gateway) { return new Checkout(gateway); }
    }

    @Test
    void noTieBreakerMeansStartupFailure() {
        new ApplicationContextRunner().withUserConfiguration(Ambiguous.class).run(ctx -> {
            Throwable failure = ctx.getStartupFailure();
            Throwable root = failure;
            while (root.getCause() != null) root = root.getCause();
            System.out.println("context failed: " + root.getClass().getSimpleName());
            System.out.println("  " + root.getMessage());
            assertThat(root).isInstanceOf(NoUniqueBeanDefinitionException.class);
        });
    }
}
