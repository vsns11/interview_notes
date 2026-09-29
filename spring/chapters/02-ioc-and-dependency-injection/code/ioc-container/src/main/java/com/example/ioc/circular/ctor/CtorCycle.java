package com.example.ioc.circular.ctor;

import org.springframework.stereotype.Component;

public final class CtorCycle {
    private CtorCycle() {}
    @Component public static class OrderService {
        public OrderService(CustomerService customers) {}
    }
    @Component public static class CustomerService {
        public CustomerService(OrderService orders) {}
    }
}
