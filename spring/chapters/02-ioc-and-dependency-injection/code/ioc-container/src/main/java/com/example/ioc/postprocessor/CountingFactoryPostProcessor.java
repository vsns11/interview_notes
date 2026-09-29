package com.example.ioc.postprocessor;

import com.example.ioc.Trace;
import org.springframework.beans.factory.config.BeanFactoryPostProcessor;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.stereotype.Component;

/** Runs after all bean DEFINITIONS are loaded but before any normal bean is created. */
@Component
public class CountingFactoryPostProcessor implements BeanFactoryPostProcessor {
    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory bf) {
        boolean greeterCreated = bf.containsSingleton("friendlyGreeter");
        Trace.add("BeanFactoryPostProcessor: friendlyGreeter defined=" + bf.containsBeanDefinition("friendlyGreeter")
                + ", created=" + greeterCreated);
    }
}
