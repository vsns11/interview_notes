package ch18.proofs;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/** Q: "What happens when you inject a prototype bean into a singleton?" */
class ScopeProofTests {

    static class ShoppingCart { }                       // meant to be "new per use"

    static class CheckoutService {                      // a singleton
        final ShoppingCart injectedOnce;                // injected ONE time, at creation
        final ObjectProvider<ShoppingCart> carts;       // asks the container each time
        CheckoutService(ShoppingCart cart, ObjectProvider<ShoppingCart> carts) {
            this.injectedOnce = cart;
            this.carts = carts;
        }
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {
        @Bean @Scope("prototype") ShoppingCart cart() { return new ShoppingCart(); }
        @Bean CheckoutService checkout(ShoppingCart cart, ObjectProvider<ShoppingCart> carts) {
            return new CheckoutService(cart, carts);
        }
    }

    @Test
    void prototypeInjectedIntoSingletonIsCreatedOnlyOnce() {
        new ApplicationContextRunner().withUserConfiguration(Config.class).run(ctx -> {
            CheckoutService a = ctx.getBean(CheckoutService.class);
            CheckoutService b = ctx.getBean(CheckoutService.class);
            System.out.println("same CheckoutService both times?      " + (a == b));
            System.out.println("same injected cart both times?        " + (a.injectedOnce == b.injectedOnce));
            System.out.println("ObjectProvider.getObject() twice same? "
                    + (a.carts.getObject() == a.carts.getObject()));
            assertThat(a).isSameAs(b);
            assertThat(a.injectedOnce).isSameAs(b.injectedOnce);       // the "bug"
            assertThat(a.carts.getObject()).isNotSameAs(a.carts.getObject()); // the fix
        });
    }
}
