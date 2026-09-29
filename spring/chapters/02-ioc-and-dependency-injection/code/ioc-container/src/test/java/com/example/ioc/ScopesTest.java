package com.example.ioc;

import com.example.ioc.scopes.CheckoutService;
import com.example.ioc.scopes.PriceList;
import com.example.ioc.scopes.ShoppingCart;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class ScopesTest {

    @Test
    void singletonVsPrototype() {
        Trace.clear();
        try (var ctx = new AnnotationConfigApplicationContext("com.example.ioc.scopes")) {
            System.out.printf("singleton: PriceList #%d, #%d%n",
                    ctx.getBean(PriceList.class).id, ctx.getBean(PriceList.class).id);
            System.out.printf("prototype: ShoppingCart #%d, #%d%n",
                    ctx.getBean(ShoppingCart.class).id, ctx.getBean(ShoppingCart.class).id);

            CheckoutService checkout = ctx.getBean(CheckoutService.class);
            System.out.printf("prototype injected into singleton: #%d, #%d (stuck!)%n",
                    checkout.cartIdFromField(), checkout.cartIdFromField());
            System.out.printf("via ObjectProvider:                #%d, #%d (fresh)%n",
                    checkout.cartIdFromProvider(), checkout.cartIdFromProvider());

            assertThat(ctx.getBean(PriceList.class)).isSameAs(ctx.getBean(PriceList.class));
            assertThat(ctx.getBean(ShoppingCart.class)).isNotSameAs(ctx.getBean(ShoppingCart.class));
            assertThat(checkout.cartIdFromField()).isEqualTo(checkout.cartIdFromField());
            assertThat(checkout.cartIdFromProvider()).isNotEqualTo(checkout.cartIdFromProvider());
        }
        Trace.print("destroy callbacks on close");
        assertThat(Trace.lines()).hasSize(1).allMatch(l -> l.startsWith("PriceList"));
    }
}
