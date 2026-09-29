package com.example.ioc;

import com.example.ioc.lifecycle.LifecycleConfig;
import org.junit.jupiter.api.Test;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import static org.assertj.core.api.Assertions.assertThat;

class LifecycleTest {

    @Test
    void callbacksRunInAFixedOrder() {
        Trace.clear();
        var ctx = new AnnotationConfigApplicationContext(LifecycleConfig.class);
        Trace.add("----- application running -----");
        ctx.close();
        Trace.print("bean lifecycle");
        assertThat(Trace.lines()).containsSubsequence(
                "constructor", "setter injection", "BeanNameAware.setBeanName(cacheWarmer)",
                "@PostConstruct", "InitializingBean.afterPropertiesSet", "@Bean(initMethod) customInit",
                "BeanPostProcessor.after(cacheWarmer)", "SmartLifecycle.start (Poller)", "ContextRefreshedEvent",
                "ContextClosedEvent", "SmartLifecycle.stop (Poller)",
                "@PreDestroy", "DisposableBean.destroy", "@Bean(destroyMethod) customDestroy");
    }
}
