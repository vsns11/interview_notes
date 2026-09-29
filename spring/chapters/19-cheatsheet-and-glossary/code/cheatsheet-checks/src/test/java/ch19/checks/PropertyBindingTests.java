package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.Binder;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.boot.context.properties.source.ConfigurationPropertySources;
import org.springframework.boot.context.properties.source.MapConfigurationPropertySource;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;
import org.springframework.util.unit.DataSize;

/** Sheet 19.5: relaxed binding of environment variables, Duration and DataSize conversion, @DefaultValue. */
class PropertyBindingTests {

    /** A @ConfigurationProperties-style record: bound through its constructor. */
    record ShopProperties(String name, @DefaultValue("30s") Duration checkoutTimeout, @DefaultValue("5MB") DataSize maxUpload) { }

    /** Binder over fake OS environment variables (the same mapping Boot applies to real ones). */
    private static Binder envBinder(Map<String, Object> envVars) {
        StandardEnvironment env = new StandardEnvironment();
        env.getPropertySources().replace(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME,
                new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME, envVars));
        ConfigurationPropertySources.attach(env);
        return Binder.get(env);
    }

    @Test
    void environmentVariables() {
        Binder canonical = envBinder(Map.of("SHOP_CHECKOUTTIMEOUT", "2m", "SHOP_PAYMENT_RETRIES", "4", "SHOP_CURRENCIES_0", "USD"));
        Binder legacy = envBinder(Map.of("SHOP_CHECKOUT_TIMEOUT", "3m"));
        Duration timeout = canonical.bind("shop.checkout-timeout", Duration.class).get();
        int retries = canonical.bind("shop.payment.retries", Integer.class).get();
        String firstCurrency = canonical.bind("shop.currencies[0]", String.class).get();
        Duration legacyTimeout = legacy.bind("shop.checkout-timeout", Duration.class).get();
        System.out.println("SHOP_CHECKOUTTIMEOUT=2m   -> shop.checkout-timeout = " + timeout);
        System.out.println("SHOP_PAYMENT_RETRIES=4    -> shop.payment.retries  = " + retries);
        System.out.println("SHOP_CURRENCIES_0=USD     -> shop.currencies[0]    = " + firstCurrency);
        System.out.println("SHOP_CHECKOUT_TIMEOUT=3m  -> shop.checkout-timeout = " + legacyTimeout + " (legacy '-' -> '_' form, also accepted)");
        assertThat(timeout).isEqualTo(Duration.ofMinutes(2));
        assertThat(retries).isEqualTo(4);
        assertThat(firstCurrency).isEqualTo("USD");
        assertThat(legacyTimeout).isEqualTo(Duration.ofMinutes(3));
    }

    @Test
    void durationsAndDataSizes() {
        Binder b = new Binder(new MapConfigurationPropertySource(Map.of(
                "a.duration", "45s", "a.plain-duration", "500", "a.iso", "PT1H",
                "a.size", "10MB", "a.plain-size", "1024")));
        Duration d = b.bind("a.duration", Duration.class).get();
        Duration plainD = b.bind("a.plain-duration", Duration.class).get();
        Duration iso = b.bind("a.iso", Duration.class).get();
        DataSize s = b.bind("a.size", DataSize.class).get();
        DataSize plainS = b.bind("a.plain-size", DataSize.class).get();
        System.out.println("Duration '45s' -> " + d + ", '500' -> " + plainD.toMillis() + " ms, 'PT1H' -> " + iso);
        System.out.println("DataSize '10MB' -> " + s.toBytes() + " bytes, '1024' -> " + plainS.toBytes() + " bytes");
        assertThat(d).isEqualTo(Duration.ofSeconds(45));
        assertThat(plainD).isEqualTo(Duration.ofMillis(500));          // plain numbers = milliseconds
        assertThat(iso).isEqualTo(Duration.ofHours(1));
        assertThat(s.toBytes()).isEqualTo(10L * 1024 * 1024);            // MB = 1024 * 1024 bytes
        assertThat(plainS.toBytes()).isEqualTo(1024);                    // plain numbers = bytes
    }

    @Test
    void recordBindingWithDefaults() {
        Binder b = new Binder(new MapConfigurationPropertySource(Map.of("shop.name", "Corner Shop")));
        ShopProperties p = b.bind("shop", ShopProperties.class).get();
        System.out.println("record bound with @DefaultValue: " + p);
        assertThat(p.checkoutTimeout()).isEqualTo(Duration.ofSeconds(30));
        assertThat(p.maxUpload()).isEqualTo(DataSize.ofMegabytes(5));
    }
}
