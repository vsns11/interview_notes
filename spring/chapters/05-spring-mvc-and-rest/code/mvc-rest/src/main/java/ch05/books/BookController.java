package ch05.books;

import ch05.errors.BookNotFoundException;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.groups.Default;
import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

@RestController                         // = @Controller + @ResponseBody on every method
@RequestMapping("/api/books")           // common path prefix for the class
public class BookController {

    private final BookRepository repo;

    public BookController(BookRepository repo) { this.repo = repo; }

    // GET /api/books?page=0&size=2&author=Martin%20Fowler
    @GetMapping
    public PageResponse<Book> list(@RequestParam(defaultValue = "0") @Min(0) int page,
                                   @RequestParam(defaultValue = "2") @Min(1) @Max(50) int size,
                                   @RequestParam(required = false) String author) {
        List<Book> all = repo.findAll(author);
        List<Book> slice = all.stream().skip((long) page * size).limit(size).toList();
        int totalPages = (all.size() + size - 1) / size;
        return new PageResponse<>(slice, page, size, all.size(), totalPages);
    }

    // GET /api/books/3
    @GetMapping("/{id:\\d+}")      // regex: only digits, so "/export" never lands here
    public Book get(@PathVariable long id,
                    @RequestHeader(name = "X-Client", defaultValue = "unknown") String client) {
        return repo.findById(id).orElseThrow(() -> new BookNotFoundException(id));
    }

    // POST /api/books  {json}  -> 201 Created + Location header
    @PostMapping
    public ResponseEntity<Book> create(
            @Validated({BookRequest.OnCreate.class, Default.class}) @RequestBody BookRequest request) {
        Book saved = repo.save(request);
        URI location = ServletUriComponentsBuilder.fromCurrentRequest()
                .path("/{id}").buildAndExpand(saved.id()).toUri();
        return ResponseEntity.created(location).body(saved);
    }

    // PUT /api/books/3  {json}  -> 200 with the new state (isbn optional: Default group only)
    @PutMapping("/{id:\\d+}")
    public Book update(@PathVariable long id, @Validated @RequestBody BookRequest request) {
        return repo.update(id, request).orElseThrow(() -> new BookNotFoundException(id));
    }

    // DELETE /api/books/3 -> 204 No Content
    @DeleteMapping("/{id:\\d+}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable long id) {
        if (!repo.delete(id)) throw new BookNotFoundException(id);
    }

    // Same URL, two representations: chosen by the Accept header
    @GetMapping(value = "/export", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<Book> exportJson() {
        return repo.findAll(null);
    }

    @GetMapping(value = "/export", produces = "text/csv")
    public String exportCsv() {
        return repo.findAll(null).stream()
                .map(b -> b.id() + "," + b.title() + "," + b.year())
                .collect(Collectors.joining("\n", "id,title,year\n", "\n"));
    }
}
