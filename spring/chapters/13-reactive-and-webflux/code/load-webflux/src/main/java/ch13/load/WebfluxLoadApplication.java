package ch13.load;

import java.time.Duration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@SpringBootApplication
@RestController
public class WebfluxLoadApplication {
    public static void main(String[] args) { SpringApplication.run(WebfluxLoadApplication.class, args); }

    /** I/O-bound handler, non-blocking: a timer completes the response after 100 ms; no thread waits. */
    @GetMapping("/io")
    Mono<String> io(@RequestParam(defaultValue = "100") long ms) {
        return Mono.delay(Duration.ofMillis(ms)).map(x -> "ok (non-blocking)");
    }

    /** ANTI-PATTERN: blocking inside a reactive handler parks an event-loop thread. */
    @GetMapping("/io-blocking")
    Mono<String> ioBlocking(@RequestParam(defaultValue = "100") long ms) throws InterruptedException {
        Thread.sleep(ms);   // runs on reactor-http-nio-N: that loop serves nobody else meanwhile
        return Mono.just("ok (blocked " + Thread.currentThread().getName().replaceAll("-\\\\d+$", "-N") + ")");
    }
}
