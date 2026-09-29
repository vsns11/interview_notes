package com.example.config;

import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.core.NestedExceptionUtils;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ValidationTest {

    @Test
    void invalidSettingsStopTheApplicationAtStartup() {
        assertThatThrownBy(() -> Apps.start("--shop.payment.retries=9", "--shop.name= "))
                .satisfies(e -> {
                    Throwable root = NestedExceptionUtils.getMostSpecificCause(e);
                    System.out.println(root.getClass().getSimpleName() + ":");
                    root.getMessage().lines().filter(l -> l.contains("Field error")).sorted()
                            .forEach(l -> System.out.println("  " + l.replaceAll(";.*default message", " ->")));
                })
                .rootCause().isInstanceOf(BindValidationException.class)
                .hasMessageContaining("shop.payment.retries");
    }
}
