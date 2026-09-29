package com.example.config;

import java.util.Arrays;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ProfilesTest {

    @Test
    void profileGroupActivatesSeveralProfilesAndTheirDocuments() {
        try (var ctx = Apps.start("--spring.profiles.active=production")) {
            var p = ctx.getBean(ShopProperties.class).payment();
            String[] active = ctx.getEnvironment().getActiveProfiles();
            System.out.println("active profiles = " + Arrays.toString(active));
            System.out.println("payment         = " + p);
            assertThat(active).containsExactly("production", "prod", "metrics");
            assertThat(p.retries()).isEqualTo(5);
        }
    }

    @Test
    void importedFileIsPartOfTheEnvironment() {
        try (var ctx = Apps.start()) {
            System.out.println("shop.owner from owner.properties = " + ctx.getEnvironment().getProperty("shop.owner"));
            assertThat(ctx.getEnvironment().getProperty("shop.owner")).isEqualTo("Ada");
        }
    }
}
