package ch09.jdbc;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jdbc.repository.config.EnableJdbcAuditing;

@SpringBootApplication
@EnableJdbcAuditing          // fills @CreatedDate / @LastModifiedDate fields
public class DataJdbcApplication {
    public static void main(String[] args) {
        SpringApplication.run(DataJdbcApplication.class, args);
    }
}
