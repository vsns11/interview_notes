package ch16.config;

import static org.assertj.core.api.Assertions.assertThat;

import ch16.config.client.OrderSettings;
import ch16.config.client.OrdersApplication;
import ch16.config.server.ConfigServerApplication;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.web.server.context.WebServerApplicationContext;
import org.springframework.cloud.context.refresh.ContextRefresher;
import org.springframework.context.ConfigurableApplicationContext;

class ConfigServerAndClientTests {

    @TempDir Path repo;   // plays the role of the Git repository with config files

    @Test
    void clientGetsLayeredConfigAndRefreshes() throws Exception {
        Files.writeString(repo.resolve("application.properties"),      // shared by ALL services
                "shared.timeout=5s\norders.greeting=hello from application.properties\n");
        Files.writeString(repo.resolve("orders.properties"),           // service "orders", any profile
                "orders.greeting=hello from orders.properties\norders.discount-percent=10\n");
        Files.writeString(repo.resolve("orders-prod.properties"),      // service "orders", profile prod
                "orders.discount-percent=15\n");

        try (ConfigurableApplicationContext server = new SpringApplicationBuilder(ConfigServerApplication.class)
                .run("--server.port=0", "--spring.main.banner-mode=off",
                        "--spring.profiles.active=native",
                        "--spring.cloud.config.server.native.search-locations=file:" + repo + "/",
                        "--spring.cloud.config.enabled=false")) {     // the server is not itself a client
            int port = ((WebServerApplicationContext) server).getWebServer().getPort();

            String json = HttpClient.newHttpClient().send(
                    HttpRequest.newBuilder(URI.create("http://localhost:" + port + "/orders/prod")).build(),
                    HttpResponse.BodyHandlers.ofString()).body();
            System.out.println("GET /orders/prod -> property sources, highest priority first:");
            java.util.regex.Pattern.compile("\"name\":\"file:[^\"]*/([^/\"]+)\"").matcher(json).results()
                    .forEach(m -> System.out.println("   " + m.group(1)));

            try (ConfigurableApplicationContext client = new SpringApplicationBuilder(OrdersApplication.class)
                    .web(WebApplicationType.NONE)
                    .run("--spring.main.banner-mode=off",
                            "--spring.application.name=orders",
                            "--spring.profiles.active=prod",
                            "--spring.config.import=configserver:http://localhost:" + port,
                            // only needed here because the server jar is on the same classpath
                            // (it switches the client off by default)
                            "--spring.cloud.config.enabled=true")) {
                OrderSettings settings = client.getBean(OrderSettings.class);
                System.out.println("client sees:    " + settings);
                assertThat(settings.toString()).startsWith("discount=15%, greeting='hello from orders.properties'");

                // ops change the file in the repo ...
                Files.writeString(repo.resolve("orders-prod.properties"), "orders.discount-percent=20\n");
                // ... and trigger a refresh (normally: POST /actuator/refresh, or Spring Cloud Bus for all instances)
                Set<String> changed = client.getBean(ContextRefresher.class).refresh();
                System.out.println("refresh changed " + changed);
                System.out.println("after refresh:  " + client.getBean(OrderSettings.class));
                assertThat(client.getBean(OrderSettings.class).toString()).startsWith("discount=20%");
            }
        }
    }
}
