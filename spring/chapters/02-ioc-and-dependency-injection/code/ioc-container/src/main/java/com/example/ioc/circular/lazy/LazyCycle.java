package com.example.ioc.circular.lazy;

import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Component;

public final class LazyCycle {
    private LazyCycle() {}
    @Component public static class OrderService {
        public final CustomerService customers;
        public OrderService(@Lazy CustomerService customers) {  // gets a proxy, real bean resolved on first call
            this.customers = customers;
        }
    }
    @Component public static class CustomerService {
        public final OrderService orders;
        public CustomerService(OrderService orders) { this.orders = orders; }
        public String name() { return "customers"; }
    }
}
