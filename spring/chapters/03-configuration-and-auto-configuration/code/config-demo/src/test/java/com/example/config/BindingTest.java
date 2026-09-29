package com.example.config;

import java.time.Duration;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.util.unit.DataSize;

import static org.assertj.core.api.Assertions.assertThat;

class BindingTest {

    @Test
    void typeSafeBindingIntoARecord() {
        try (var ctx = Apps.start()) {
            ShopProperties p = ctx.getBean(ShopProperties.class);
            System.out.println("@ConfigurationProperties -> " + p);
            System.out.println("@Value                   -> " + ctx.getBean(ValueStyle.class));
            assertThat(p.checkoutTimeout()).isEqualTo(Duration.ofSeconds(45));
            assertThat(p.maxUpload()).isEqualTo(DataSize.ofMegabytes(10));
            assertThat(p.discounts()).containsEntry("gold", 15);
            assertThat(p.payment().gatewayUrl()).isEqualTo("https://pay.example.test");  // @DefaultValue
        }
    }

    @Test
    void relaxedBindingFromEnvironmentVariables() {
        try (var ctx = Apps.start(Map.of("SHOP_PAYMENT_RETRIES", "4", "SHOP_CHECKOUTTIMEOUT", "2m"))) {
            ShopProperties p = ctx.getBean(ShopProperties.class);
            System.out.println("SHOP_PAYMENT_RETRIES=4, SHOP_CHECKOUTTIMEOUT=2m -> retries="
                    + p.payment().retries() + ", checkoutTimeout=" + p.checkoutTimeout());
            assertThat(p.payment().retries()).isEqualTo(4);
            assertThat(p.checkoutTimeout()).isEqualTo(Duration.ofMinutes(2));
        }
    }
}
