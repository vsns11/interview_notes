package ch16.clients;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.web.service.registry.ImportHttpServices;

@SpringBootApplication
@EnableFeignClients                                                        // scan for @FeignClient
@ImportHttpServices(group = "inventory", types = InventoryHttpClient.class) // group name = service id
public class ClientsApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClientsApplication.class, args);
    }
}
