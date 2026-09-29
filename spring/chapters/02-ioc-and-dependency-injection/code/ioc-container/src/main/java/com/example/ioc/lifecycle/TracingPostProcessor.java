package com.example.ioc.lifecycle;

import com.example.ioc.Trace;
import org.springframework.beans.factory.config.BeanPostProcessor;

/** Sees every bean before and after its init callbacks. */
public class TracingPostProcessor implements BeanPostProcessor {
    @Override
    public Object postProcessBeforeInitialization(Object bean, String name) {
        if (bean instanceof CacheWarmer) Trace.add("BeanPostProcessor.before(" + name + ")");
        return bean;
    }
    @Override
    public Object postProcessAfterInitialization(Object bean, String name) {
        if (bean instanceof CacheWarmer) Trace.add("BeanPostProcessor.after(" + name + ")");
        return bean;                      // could return a proxy/wrapper instead
    }
}
