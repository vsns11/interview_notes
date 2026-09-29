package ch05.books;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicLong;
import org.springframework.stereotype.Repository;

/** In-memory store so the chapter can focus on the web layer (databases: chapters 7-8). */
@Repository
public class BookRepository {

    private final Map<Long, Book> books = new ConcurrentHashMap<>();
    private final AtomicLong ids = new AtomicLong();

    public BookRepository() {
        reset();
    }

    public void reset() {
        books.clear();
        ids.set(0);
        save(new BookRequest("Clean Code", "Robert C. Martin", "9780132350884", 2008));
        save(new BookRequest("Effective Java", "Joshua Bloch", "9780134685991", 2018));
        save(new BookRequest("Refactoring", "Martin Fowler", "9780134757599", 2018));
        save(new BookRequest("Domain-Driven Design", "Eric Evans", "9780321125217", 2003));
        save(new BookRequest("Release It!", "Michael T. Nygard", "9781680502398", 2018));
    }

    public Book save(BookRequest r) {
        long id = ids.incrementAndGet();
        Book b = new Book(id, r.title(), r.author(), r.isbn(), r.year(), LocalDate.of(2026, 1, 15));
        books.put(id, b);
        return b;
    }

    public Optional<Book> update(long id, BookRequest r) {
        return findById(id).map(old -> {
            String isbn = r.isbn() != null ? r.isbn() : old.isbn();
            Book b = new Book(id, r.title(), r.author(), isbn, r.year(), old.addedOn());
            books.put(id, b);
            return b;
        });
    }

    public Optional<Book> findById(long id) { return Optional.ofNullable(books.get(id)); }

    public boolean delete(long id) { return books.remove(id) != null; }

    public List<Book> findAll(String author) {
        return books.values().stream()
                .filter(b -> author == null || b.author().equalsIgnoreCase(author))
                .sorted(Comparator.comparing(Book::id))
                .toList();
    }
}
