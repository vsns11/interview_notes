package ch05.versioning;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/greeting")
public class GreetingController {

    // Records keep a fixed JSON field order (Map.of has no stable iteration order).
    public record GreetingV1(String message) {}
    public record GreetingV1_1(String message, String language) {}
    public record GreetingV2(String text, String lang) {}

    @GetMapping(version = "1.0")            // exactly version 1.0
    public GreetingV1 v1() {
        return new GreetingV1("Hello");
    }

    @GetMapping(version = "1.1+")           // baseline: 1.1 and every later version...
    public GreetingV1_1 v1_1() {
        return new GreetingV1_1("Hello", "en");
    }

    @GetMapping(version = "2.0")            // ...until a more specific mapping takes over
    public GreetingV2 v2() {
        return new GreetingV2("Hello", "en");
    }
}
