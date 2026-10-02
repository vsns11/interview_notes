package com.example.greeting.autoconfigure;

import com.example.greeting.core.GreetingService;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBooleanProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

@AutoConfiguration                                              // a @Configuration(proxyBeanMethods = false) for Boot
@ConditionalOnClass(GreetingService.class)                      // 1. only if the library is on the classpath
@ConditionalOnBooleanProperty(name = "greeting.enabled", matchIfMissing = true) // 2. unless switched off
@EnableConfigurationProperties(GreetingProperties.class)        // 3. bind greeting.* into GreetingProperties
public class GreetingAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean                                   // 4. back off if the user defined their own
    GreetingService greetingService(GreetingProperties props) {
        return new GreetingService(props.template());
    }
}
