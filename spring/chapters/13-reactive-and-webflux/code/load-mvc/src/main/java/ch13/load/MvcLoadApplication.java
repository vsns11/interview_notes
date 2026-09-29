package ch13.load;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class MvcLoadApplication {
    public static void main(String[] args) { SpringApplication.run(MvcLoadApplication.class, args); }

    /** I/O-bound handler: the thread WAITS 100 ms, like a JDBC query or a blocking HTTP call. */
    @GetMapping("/io")
    String io(@RequestParam(defaultValue = "100") long ms) throws InterruptedException {
        Thread.sleep(ms);
        return "ok on " + (Thread.currentThread().isVirtual() ? "virtual" : "platform") + " thread";
    }
}
