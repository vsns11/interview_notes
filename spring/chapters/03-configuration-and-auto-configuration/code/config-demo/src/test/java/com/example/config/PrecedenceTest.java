package com.example.config;

import java.util.Map;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PrecedenceTest {

    static String shopName(Map<String, Object> env, String... args) {
        try (var ctx = Apps.start(env, args)) { return ctx.getBean(ShopProperties.class).name(); }
    }

    @Test
    void laterSourcesWin() {
        String fromFile = shopName(Map.of());
        String fromProfile = shopName(Map.of(), "--spring.profiles.active=dev");
        String fromEnv = shopName(Map.of("SHOP_NAME", "Env Shop"), "--spring.profiles.active=dev");
        String fromSysProp;
        System.setProperty("shop.name", "SysProp Shop");
        try {
            fromSysProp = shopName(Map.of("SHOP_NAME", "Env Shop"));
        } finally {
            System.clearProperty("shop.name");
        }
        System.setProperty("shop.name", "SysProp Shop");
        String fromArgs;
        try {
            fromArgs = shopName(Map.of("SHOP_NAME", "Env Shop"), "--shop.name=CLI Shop");
        } finally {
            System.clearProperty("shop.name");
        }
        System.out.println("1 application.yaml only          -> " + fromFile);
        System.out.println("2 + application-dev.yaml         -> " + fromProfile);
        System.out.println("3 + env SHOP_NAME                -> " + fromEnv);
        System.out.println("4 + -Dshop.name (system prop)    -> " + fromSysProp);
        System.out.println("5 + --shop.name (command line)   -> " + fromArgs);
        assertThat(fromFile).isEqualTo("Corner Shop");
        assertThat(fromProfile).isEqualTo("Corner Shop (DEV)");
        assertThat(fromEnv).isEqualTo("Env Shop");
        assertThat(fromSysProp).isEqualTo("SysProp Shop");
        assertThat(fromArgs).isEqualTo("CLI Shop");
    }

    @Test
    void propertySourcesInSearchOrder() {
        try (var ctx = Apps.start("--spring.profiles.active=dev")) {
            System.out.println("Environment property sources (first wins):");
            ctx.getEnvironment().getPropertySources().forEach(ps -> System.out.println("  " + ps.getName()));
        }
    }
}
