package ch12.tc;

import org.springframework.boot.SpringApplication;

/** Run this main() (or `mvn spring-boot:test-run`) to start the app locally against a container. */
public class TestTcApplication {
    public static void main(String[] args) {
        SpringApplication.from(TcApplication::main).with(ContainersConfig.class).run(args);
    }
}
