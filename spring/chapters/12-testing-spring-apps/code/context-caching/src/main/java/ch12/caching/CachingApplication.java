package ch12.caching;

import java.util.concurrent.atomic.AtomicInteger;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
public class CachingApplication {
    public static void main(String[] args) { SpringApplication.run(CachingApplication.class, args); }

    /** Simulates slow startup work (warming a cache, loading a model...): 400 ms per context. */
    @Component
    public static class SlowStartup {
        public static final AtomicInteger CREATED = new AtomicInteger();
        public SlowStartup() throws InterruptedException {
            Thread.sleep(400);
            CREATED.incrementAndGet();
        }
    }

    @Service
    public static class GreetingService {
        public String greet(String name) { return "Hello " + name; }
    }

    @RestController
    public static class GreetingController {
        private final GreetingService service;
        private final boolean discounts;
        GreetingController(GreetingService service, @Value("${feature.discounts}") boolean discounts) {
            this.service = service;
            this.discounts = discounts;
        }
        @GetMapping("/greet")
        String greet() { return service.greet("reader") + (discounts ? " (10% off)" : ""); }
    }
}
