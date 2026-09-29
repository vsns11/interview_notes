package com.example.config;

import java.time.Duration;
import java.util.List;
import java.util.Map;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.context.properties.bind.DefaultValue;
import org.springframework.util.unit.DataSize;
import org.springframework.validation.annotation.Validated;

/**
 * Everything under "shop.*", bound once at startup into an immutable record.
 *
 * @param name            display name of the shop
 * @param checkoutTimeout how long a checkout may take
 * @param maxUpload       largest product image accepted
 * @param currencies      accepted currency codes
 * @param discounts       discount percent per customer tier
 * @param payment         payment gateway settings
 */
@ConfigurationProperties("shop")
@Validated
public record ShopProperties(
        @NotBlank String name,
        @DefaultValue("30s") Duration checkoutTimeout,
        @DefaultValue("5MB") DataSize maxUpload,
        @DefaultValue("USD") List<String> currencies,
        Map<String, Integer> discounts,
        @Valid @DefaultValue Payment payment) {

    /**
     * @param gatewayUrl base URL of the payment gateway
     * @param retries    retry attempts for a failed charge
     */
    public record Payment(
            @NotBlank @DefaultValue("https://pay.example.test") String gatewayUrl,
            @Min(0) @Max(5) @DefaultValue("3") int retries) {
    }
}
