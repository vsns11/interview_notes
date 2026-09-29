package ch06;

import ch06.client.CatalogApi;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableResilientMethods                                       // for @Retryable (Framework 7)
@ImportHttpServices(group = "catalog", types = CatalogApi.class)   // creates a CatalogApi bean
public class ClientsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientsApplication.class, args);
    }
}
