package com.example.ioc.circular.field;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

public final class FieldCycle {
    private FieldCycle() {}
    @Component public static class OrderService {
        @Autowired public CustomerService customers;
    }
    @Component public static class CustomerService {
        @Autowired public OrderService orders;
    }
}
