package ch08.jpa;

import java.util.Optional;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.data.domain.AuditorAware;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing // fills @CreatedDate / @LastModifiedDate / @CreatedBy / @LastModifiedBy
public class JpaDemosApplication {

    public static void main(String[] args) {
        SpringApplication.run(JpaDemosApplication.class, args);
    }

    /** Who is "the current user" for @CreatedBy? In a real app: read it from Spring Security. */
    @Bean
    AuditorAware<String> auditor() {
        return () -> Optional.of("system");
    }
}
