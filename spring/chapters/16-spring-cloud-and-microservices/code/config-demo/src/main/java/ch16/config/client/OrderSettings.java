package ch16.config.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

// @RefreshScope: the bean is re-created with fresh values after a refresh
@Component
@RefreshScope
public class OrderSettings {

    private final int discountPercent;
    private final String greeting;
    private final String timeout;

    public OrderSettings(@Value("${orders.discount-percent}") int discountPercent,
                         @Value("${orders.greeting}") String greeting,
                         @Value("${shared.timeout}") String timeout) {
        this.discountPercent = discountPercent;
        this.greeting = greeting;
        this.timeout = timeout;
    }

    @Override
    public String toString() {
        return "discount=" + discountPercent + "%, greeting='" + greeting + "', timeout=" + timeout;
    }
}
