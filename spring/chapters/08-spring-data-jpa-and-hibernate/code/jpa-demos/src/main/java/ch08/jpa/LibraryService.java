package ch08.jpa;

import java.math.BigDecimal;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class LibraryService {

    public record AuthorSummary(String name, int books) {}

    private final AuthorRepository authors;
    private final BookRepository books;

    public LibraryService(AuthorRepository authors, BookRepository books) {
        this.authors = authors;
        this.books = books;
    }

    /** Lazy loading works here: the persistence context stays open until the method returns. */
    @Transactional(readOnly = true)
    public AuthorSummary summary(Long authorId) {
        Author a = authors.findById(authorId).orElseThrow();
        return new AuthorSummary(a.getName(), a.getBooks().size());
    }

    /** Dirty checking: no save() call - the change is flushed as an UPDATE at commit. */
    @Transactional
    public void changePrice(Long bookId, BigDecimal newPrice) {
        books.findById(bookId).orElseThrow().setPrice(newPrice);
    }
}
