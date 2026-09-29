package com.example.config;

import java.util.Map;

import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.StandardEnvironment;

/** Starts the real application the way `java -jar app.jar args...` would, with a fake OS environment. */
final class Apps {
    private Apps() {}

    static ConfigurableApplicationContext start(Map<String, Object> osEnv, String... args) {
        var env = new StandardEnvironment() {
            @Override public Map<String, Object> getSystemEnvironment() { return osEnv; }   // pretend env vars
        };
        return new SpringApplicationBuilder(ConfigDemoApplication.class)
                .environment(env)
                .web(WebApplicationType.NONE)
                .logStartupInfo(false)
                .properties("spring.main.banner-mode=off", "logging.level.root=WARN")
                .run(args);
    }

    static ConfigurableApplicationContext start(String... args) { return start(Map.of(), args); }
}
