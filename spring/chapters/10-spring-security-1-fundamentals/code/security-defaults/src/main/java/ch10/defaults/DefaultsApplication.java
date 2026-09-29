package ch10.defaults;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

// No security configuration at all: only the starter is on the classpath.
@SpringBootApplication
public class DefaultsApplication {
    public static void main(String[] args) {
        SpringApplication.run(DefaultsApplication.class, args);
    }
}

@RestController
class HelloController {
    @GetMapping("/hello")
    String hello() {
        return "hello";
    }
}
