package com.example.greeting.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;

/**
 * Settings for the greeting starter.
 *
 * @param enabled  whether to auto-configure a GreetingService
 * @param template message template; %s is replaced by the name
 */
@ConfigurationProperties("greeting")
public record GreetingProperties(
        @DefaultValue("true") boolean enabled,
        @DefaultValue("Hello, %s!") String template) {
}
