package ch06.catalog;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * The REMOTE service our clients call. It lives in the same app only so the tests
 * can call a real HTTP server on localhost; imagine it is another team's API.
 */
@RestController
@RequestMapping("/catalog")
public class CatalogServerController {

    public record Book(Long id, String title, String author) {}

    private final Map<Long, Book> books = new ConcurrentHashMap<>(Map.of(
            1L, new Book(1L, "Release It!", "Michael T. Nygard"),
            2L, new Book(2L, "Refactoring", "Martin Fowler"),
            3L, new Book(3L, "Patterns of Enterprise Application Architecture", "Martin Fowler")));
    private final AtomicLong ids = new AtomicLong(100);
    private final AtomicInteger flakyCalls = new AtomicInteger();

    @GetMapping("/books/{id}")
    public ResponseEntity<?> get(@PathVariable long id,
                                 @RequestHeader(name = "X-Client", defaultValue = "-") String client) {
        Book b = books.get(id);
        if (b == null) {
            ProblemDetail pd = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, "No book " + id);
            pd.setTitle("Book not found");
            return ResponseEntity.status(404).body(pd);
        }
        return ResponseEntity.ok().header("X-Seen-Client", client).body(b);
    }

    @GetMapping("/books")
    public List<Book> byAuthor(@RequestParam String author) {
        return books.values().stream().filter(b -> b.author().equals(author))
                .sorted((a, c) -> Long.compare(a.id(), c.id())).toList();
    }

    @PostMapping("/books")
    public ResponseEntity<Book> create(@RequestBody Book in) {
        Book b = new Book(ids.incrementAndGet(), in.title(), in.author());
        books.put(b.id(), b);
        return ResponseEntity.created(URI.create("/catalog/books/" + b.id())).body(b);
    }

    @GetMapping("/slow")
    public String slow() throws InterruptedException {
        Thread.sleep(1500);                        // slower than the client's read timeout
        return "finally";
    }

    // fails with 503 on the first two calls after a reset, then succeeds
    @GetMapping("/flaky")
    public ResponseEntity<String> flaky() {
        int n = flakyCalls.incrementAndGet();
        return n <= 2 ? ResponseEntity.status(503).body("busy (call " + n + ")")
                      : ResponseEntity.ok("ok on call " + n);
    }

    @PostMapping("/flaky/reset")
    public void resetFlaky() { flakyCalls.set(0); }

    // Framework 7 API versioning on the server side (header X-API-Version)
    @GetMapping(path = "/greeting", version = "1.0")
    public String greetingV1() { return "v1: hello"; }

    @GetMapping(path = "/greeting", version = "2.0")
    public String greetingV2() { return "v2: hello"; }
}
