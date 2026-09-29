package ch16.resilience;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods   // Framework 7 @Retryable / @ConcurrencyLimit (for the comparison bean)
public class ResilienceApplication {
    public static void main(String[] args) {
        SpringApplication.run(ResilienceApplication.class, args);
    }
}
