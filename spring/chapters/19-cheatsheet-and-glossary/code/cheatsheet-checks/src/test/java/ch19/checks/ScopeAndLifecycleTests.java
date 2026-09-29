package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;

/** Sheet 19.7: scopes and lifecycle order. Plain Spring containers, no Boot. */
class ScopeAndLifecycleTests {

    static final List<String> LOG = new CopyOnWriteArrayList<>();

    static class Single { }

    static class Proto {
        @PreDestroy void bye() { LOG.add("Proto @PreDestroy"); }       // never called: prototypes are not destroyed
    }

    static class Holder {                                               // a singleton that needs a prototype
        final Proto proto;
        Holder(Proto proto) { this.proto = proto; }
    }

    static class Traced implements InitializingBean, DisposableBean {
        Traced()                            { LOG.add("constructor"); }
        @PostConstruct void postConstruct() { LOG.add("@PostConstruct"); }
        @Override public void afterPropertiesSet() { LOG.add("afterPropertiesSet"); }
        void customInit()                   { LOG.add("initMethod"); }
        @PreDestroy void preDestroy()       { LOG.add("@PreDestroy"); }
        @Override public void destroy()     { LOG.add("destroy()"); }
        void customDestroy()                { LOG.add("destroyMethod"); }
    }

    @Configuration(proxyBeanMethods = false)
    static class Config {
        @Bean Single single() { return new Single(); }
        @Bean @Scope("prototype") Proto proto() { return new Proto(); }
        @Bean Holder holder(Proto proto) { return new Holder(proto); }
        @Bean(initMethod = "customInit", destroyMethod = "customDestroy") Traced traced() { return new Traced(); }
    }

    @Test
    void scopes() {
        try (var ctx = new AnnotationConfigApplicationContext(Config.class)) {
            boolean singletonShared = ctx.getBean(Single.class) == ctx.getBean(Single.class);
            boolean prototypeFresh = ctx.getBean(Proto.class) != ctx.getBean(Proto.class);
            boolean stuck = ctx.getBean(Holder.class).proto == ctx.getBean(Holder.class).proto;
            System.out.println("singleton: getBean twice -> same instance?        " + singletonShared);
            System.out.println("prototype: getBean twice -> new instance?         " + prototypeFresh);
            System.out.println("prototype injected into a singleton -> same one?  " + stuck);
            assertThat(singletonShared).isTrue();
            assertThat(prototypeFresh).isTrue();
            assertThat(stuck).isTrue();
        }
    }

    @Test
    void lifecycleOrder() {
        LOG.clear();
        var ctx = new AnnotationConfigApplicationContext(Config.class);
        ctx.getBean(Proto.class);                                        // create a prototype too
        List<String> startup = List.copyOf(LOG);
        LOG.clear();
        ctx.close();
        List<String> shutdown = List.copyOf(LOG);
        System.out.println("init order    : " + startup);
        System.out.println("destroy order : " + shutdown + "  (no Proto @PreDestroy)");
        assertThat(startup).containsExactly("constructor", "@PostConstruct", "afterPropertiesSet", "initMethod");
        assertThat(shutdown).containsExactly("@PreDestroy", "destroy()", "destroyMethod");
    }
}
