package ch15.runtime;

import java.util.concurrent.CountDownLatch;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@SpringBootApplication
@RestController
public class RuntimeApplication {

    /** Counted down when a /slow request has started, so a test knows a request is "in flight". */
    public static volatile CountDownLatch slowStarted = new CountDownLatch(1);

    public static void main(String[] args) {
        SpringApplication.run(RuntimeApplication.class, args);
    }

    @GetMapping("/slow")
    public String slow(@RequestParam(defaultValue = "1000") long ms) throws InterruptedException {
        slowStarted.countDown();
        Thread.sleep(ms);                 // pretend to do real work
        return "done after " + ms + " ms";
    }

    @GetMapping("/thread")
    public String thread() {
        Thread t = Thread.currentThread();
        return (t.isVirtual() ? "virtual" : "platform") + " thread: " + t;
    }
}
