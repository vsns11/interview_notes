package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/** Sheet 19.5: which source wins, and @Value defaults/SpEL. Real SpringApplication starts (no web). */
class PropertyPrecedenceTests {

    record Values(String name, String missing, int attempts, String region) { }

    @Configuration(proxyBeanMethods = false)
    static class App {
        @Bean Values values(@Value("${shop.name}") String name,
                            @Value("${shop.missing:fallback}") String missing,      // default after the colon
                            @Value("#{${shop.retries:3} + 1}") int attempts,        // placeholder, then SpEL
                            @Value("${shop.region}") String region) {               // only in defaultProperties
            return new Values(name, missing, attempts, region);
        }
    }

    /** Starts the app with fake OS environment variables and command-line arguments. */
    private static Values start(Map<String, Object> envVars, String... args) {
        SpringApplication app = new SpringApplication(App.class);
        app.setWebApplicationType(WebApplicationType.NONE);
        app.setLogStartupInfo(false);
        app.setDefaultProperties(Map.of("shop.name", "from defaultProperties", "shop.region", "eu (default)"));
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, envVars));
        app.setEnvironment(env);
        try (var ctx = app.run(args)) {
            return ctx.getBean(Values.class);
        }
    }

    @Test
    void laterSourcesWin() {
        String a = start(Map.of()).name();
        String b = start(Map.of("SHOP_NAME", "from env var")).name();
        String c, d;
        System.setProperty("shop.name", "from -D system property");
        try {
            c = start(Map.of("SHOP_NAME", "from env var")).name();
            d = start(Map.of("SHOP_NAME", "from env var"), "--shop.name=from --command-line").name();
        } finally {
            System.clearProperty("shop.name");
        }
        System.out.println("defaultProperties + application.properties -> " + a);
        System.out.println("+ env SHOP_NAME                            -> " + b);
        System.out.println("+ -Dshop.name                              -> " + c);
        System.out.println("+ --shop.name                              -> " + d);
        assertThat(a).isEqualTo("from application.properties");
        assertThat(b).isEqualTo("from env var");
        assertThat(c).isEqualTo("from -D system property");
        assertThat(d).isEqualTo("from --command-line");
    }

    @Test
    void valueDefaultsAndSpel() {
        Values v = start(Map.of(), "--shop.retries=2");
        System.out.println("@Value ${shop.missing:fallback} -> " + v.missing() + ", #{${shop.retries:3} + 1} with retries=2 -> " + v.attempts()
                + ", key set only in defaultProperties -> " + v.region());
        assertThat(v.missing()).isEqualTo("fallback");
        assertThat(v.attempts()).isEqualTo(3);
        assertThat(v.region()).isEqualTo("eu (default)");
    }
}
