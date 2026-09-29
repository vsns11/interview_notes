package com.example.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

/** The same settings read one-by-one with @Value, for comparison. */
@Component
public class ValueStyle {
    final String name;
    final String supportEmail;
    final int maxAttempts;
    final String owner;

    ValueStyle(@Value("${shop.name}") String name,                             // required: startup fails if missing
               @Value("${shop.support-email:help@example.test}") String email, // default after the colon
               @Value("#{${shop.payment.retries:3} + 1}") int maxAttempts,     // SpEL: #{...}
               @Value("${shop.owner}") String owner) {                         // comes from an imported file
        this.name = name;
        this.supportEmail = email;
        this.maxAttempts = maxAttempts;
        this.owner = owner;
    }

    @Override public String toString() {
        return "name=" + name + ", supportEmail=" + supportEmail + ", maxAttempts=" + maxAttempts + ", owner=" + owner;
    }
}
