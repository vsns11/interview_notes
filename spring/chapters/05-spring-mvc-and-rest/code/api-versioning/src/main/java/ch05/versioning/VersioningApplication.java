package ch05.versioning;

import java.net.URI;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.accept.ApiVersionDeprecationHandler;
import org.springframework.web.accept.StandardApiVersionDeprecationHandler;

@SpringBootApplication
public class VersioningApplication {

    public static void main(String[] args) {
        SpringApplication.run(VersioningApplication.class, args);
    }

    /** Boot plugs this bean into the MVC versioning config: version 1.0 is deprecated. */
    @Bean
    ApiVersionDeprecationHandler deprecationHandler() {
        var handler = new StandardApiVersionDeprecationHandler();
        handler.configureVersion("1.0")
                .setDeprecationDate(ZonedDateTime.of(2026, 6, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setSunsetDate(ZonedDateTime.of(2027, 1, 1, 0, 0, 0, 0, ZoneOffset.UTC))
                .setDeprecationLink(URI.create("https://api.example.com/docs/migrate-to-v2"));
        return handler;
    }
}
