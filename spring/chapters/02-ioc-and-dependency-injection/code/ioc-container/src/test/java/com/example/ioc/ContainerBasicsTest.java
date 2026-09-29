package com.example.ioc;

import com.example.ioc.basics.FlatTaxPolicy;
import com.example.ioc.basics.PriceCalculator;
import com.example.ioc.basics.ShopConfig;
import com.example.ioc.basics.TaxPolicy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.support.DefaultListableBeanFactory;
import org.springframework.beans.factory.support.RootBeanDefinition;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class ContainerBasicsTest {

    @BeforeEach void reset() { Trace.clear(); }

    @Test
    void withoutSpring_youWireByHand() {
        PriceCalculator calc = new PriceCalculator(new FlatTaxPolicy());   // "new" everywhere
        System.out.println("by hand: 1000 -> " + calc.totalCents(1000));
        assertThat(calc.totalCents(1000)).isEqualTo(1200);
    }

    @Test
    void applicationContext_createsSingletonsEagerly() {
        try (var ctx = new AnnotationConfigApplicationContext(ShopConfig.class)) {
            Trace.add("-- context refreshed, now calling getBean");
            PriceCalculator calc = ctx.getBean(PriceCalculator.class);
            Trace.add("total for 1000 = " + calc.totalCents(1000));
            assertThat(ctx.getBean(PriceCalculator.class)).isSameAs(calc);   // singleton
        }
        Trace.print("ApplicationContext (eager)");
        assertThat(Trace.lines().get(0)).isEqualTo("FlatTaxPolicy created");
    }

    @Test
    void plainBeanFactory_createsBeansOnlyWhenAsked() {
        var bf = new DefaultListableBeanFactory();
        bf.registerBeanDefinition("taxPolicy", new RootBeanDefinition(FlatTaxPolicy.class));
        var calcDef = new RootBeanDefinition(PriceCalculator.class);
        calcDef.setAutowireMode(RootBeanDefinition.AUTOWIRE_CONSTRUCTOR);
        bf.registerBeanDefinition("priceCalculator", calcDef);
        Trace.add("-- definitions registered, nothing created yet");
        bf.getBean(PriceCalculator.class);
        Trace.print("Bare BeanFactory (lazy)");
        assertThat(Trace.lines().get(0)).startsWith("-- definitions registered");
        assertThat(bf.getBean(TaxPolicy.class)).isInstanceOf(FlatTaxPolicy.class);
    }
}
