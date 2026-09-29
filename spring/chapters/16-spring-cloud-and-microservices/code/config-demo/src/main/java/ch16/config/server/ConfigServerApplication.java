package ch16.config.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.config.server.EnableConfigServer;

// Serves configuration over HTTP: GET /{application}/{profile}[/{label}]
@SpringBootApplication
@EnableConfigServer
public class ConfigServerApplication {
    public static void main(String[] args) {
        // typical settings (the test passes the same ones):
        //   spring.profiles.active=native                                  -> read files instead of Git
        //   spring.cloud.config.server.native.search-locations=file:./config-repo/
        SpringApplication.run(ConfigServerApplication.class, args);
    }
}
