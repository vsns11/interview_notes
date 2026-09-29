package ch04;

import static org.assertj.core.api.Assertions.assertThat;

import ch04.aop.CallLog;
import ch04.aop.OrderService;
import ch04.proxy.DefaultGreetingService;
import ch04.proxy.GreetingService;
import ch04.proxy.PricingService;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Proxy;
import org.aopalliance.intercept.MethodInterceptor;
import org.junit.jupiter.api.Test;
import org.springframework.aop.framework.ProxyFactory;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class ProxyTypesTest {

    @Autowired OrderService orders;
    @Autowired @Qualifier("defaultGreetingService") GreetingService greeting;
    @Autowired @Qualifier("interfaceOnlyGreetingService") GreetingService interfaceOnly;
    @Autowired PricingService pricing;
    @Autowired CallLog log;

    static String describe(Object bean) {
        String kind = AopUtils.isJdkDynamicProxy(bean) ? "JDK dynamic proxy"
                    : AopUtils.isCglibProxy(bean) ? "CGLIB proxy" : "not a proxy";
        return kind + " -> " + bean.getClass().getName();
    }

    @Test
    void whatKindOfProxyDidSpringCreate() {
        System.out.println("OrderService (no interface):       " + describe(orders));
        System.out.println("DefaultGreetingService (interface): " + describe(greeting));
        System.out.println("@Proxyable(INTERFACES) bean:        " + describe(interfaceOnly));
        assertThat(AopUtils.isCglibProxy(orders)).isTrue();
        assertThat(AopUtils.isCglibProxy(greeting)).isTrue();     // Boot default: proxy-target-class=true
        assertThat(AopUtils.isJdkDynamicProxy(interfaceOnly)).isTrue();
        assertThat(interfaceOnly).isNotInstanceOf(DefaultGreetingService.class);
    }

    @Test
    void finalMethodsRunOnTheProxyItselfAndSkipAdvice() {
        log.clear();
        String normal = pricing.currencies();
        String viaFinal = pricing.currenciesFinal();
        System.out.println("normal method: " + normal);
        System.out.println("final method:  " + viaFinal);
        System.out.println("advice log:    " + log.lines());
        assertThat(normal).isEqualTo("currencies=[EUR, USD]");
        assertThat(viaFinal).isEqualTo("currencies=null");            // proxy's own, never-initialised field
        assertThat(log.lines()).containsExactly("trace PricingService.currencies()");
    }

    @Test
    void aHandMadeJdkProxy() {
        GreetingService target = new DefaultGreetingService();
        InvocationHandler handler = (proxy, method, args) -> {
            System.out.println("  [handler] before " + method.getName());
            Object result = method.invoke(target, args);      // delegate to the real object
            System.out.println("  [handler] after  " + method.getName());
            return result;
        };
        GreetingService proxy = (GreetingService) Proxy.newProxyInstance(
                GreetingService.class.getClassLoader(), new Class<?>[] {GreetingService.class}, handler);
        String out = proxy.greet("Ada");
        System.out.println("result = " + out + ", proxy class = " + proxy.getClass().getSimpleName());
        assertThat(out).isEqualTo("Hello, Ada");
    }

    @Test
    void springProxyFactoryChoosesJdkOrCglib() {
        MethodInterceptor upper = inv -> ((String) inv.proceed()).toUpperCase();

        ProxyFactory jdk = new ProxyFactory(new DefaultGreetingService());
        jdk.addInterface(GreetingService.class);   // interfaces known + proxyTargetClass=false -> JDK
        jdk.addAdvice(upper);
        GreetingService p1 = (GreetingService) jdk.getProxy();

        ProxyFactory cglib = new ProxyFactory(new DefaultGreetingService());
        cglib.setProxyTargetClass(true);           // force a subclass
        cglib.addAdvice(upper);
        DefaultGreetingService p2 = (DefaultGreetingService) cglib.getProxy();

        System.out.println("factory 1: " + describe(p1) + " says " + p1.greet("Bob"));
        System.out.println("factory 2: " + describe(p2) + " says " + p2.greet("Bob"));
        assertThat(AopUtils.isJdkDynamicProxy(p1)).isTrue();
        assertThat(AopUtils.isCglibProxy(p2)).isTrue();
        assertThat(p1.greet("Bob")).isEqualTo("HELLO, BOB");
    }
}
