package com.example.ioc.definitions;

import com.example.ioc.definitions.Services.OrderService;
import com.example.ioc.definitions.Services.PaymentService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)   // "lite" mode: no CGLIB subclass
public class LiteConfig {
    @Bean AuditLog auditLog() { return new AuditLog(); }
    @Bean OrderService orderService() { return new OrderService(auditLog()); }       // plain Java call!
    @Bean PaymentService paymentService() { return new PaymentService(auditLog()); } // plain Java call!
}
