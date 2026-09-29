package ch09.nosql;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class NoSqlApplication {
    public static void main(String[] args) {
        SpringApplication.run(NoSqlApplication.class, args);
    }
}
