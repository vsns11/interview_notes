package com.example.config;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan          // registers every @ConfigurationProperties class in this package tree
public class ConfigDemoApplication {
    public static void main(String[] args) {
        var ctx = SpringApplication.run(ConfigDemoApplication.class, args);
        System.out.println("shop.name = " + ctx.getBean(ShopProperties.class).name());
    }
}
