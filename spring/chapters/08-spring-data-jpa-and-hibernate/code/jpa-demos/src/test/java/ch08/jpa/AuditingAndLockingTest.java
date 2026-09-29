package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.transaction.support.TransactionTemplate;

/** No test transaction: every repository call commits, like two real users would. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class AuditingAndLockingTest {

    @Autowired AuthorRepository authors;
    @Autowired BookRepository books;
    @Autowired LibraryService library;
    @Autowired TransactionTemplate tx;
    @Autowired EntityManagerFactory emf;
    Long bookId;

    @BeforeEach
    void createBook() {
        bookId = tx.execute(status -> {
            Author asimov = authors.findById(4L).orElseThrow();
            Book book = new Book("The Gods Themselves", new BigDecimal("9.50"));
            asimov.addBook(book);
            return books.save(book).getId();
        });
    }

    @AfterEach
    void deleteBook() {
        books.deleteById(bookId);
    }

    @Test
    void auditingFillsTheColumns() {
        Book b = books.findById(bookId).orElseThrow();
        System.out.println("created: by=" + b.getCreatedBy() + " at=" + (b.getCreatedAt() != null)
                + " version=" + b.getVersion());
        Long before = b.getVersion();
        library.changePrice(bookId, new BigDecimal("10.50")); // dirty checking inside a @Transactional service
        Book after = books.findById(bookId).orElseThrow();
        System.out.println("after changePrice: price=" + after.getPrice() + " version=" + after.getVersion()
                + " updatedAt moved? " + after.getUpdatedAt().isAfter(b.getUpdatedAt()));
        assertThat(after.getVersion()).isEqualTo(before + 1);
        assertThat(after.getCreatedBy()).isEqualTo("system");
    }

    @Test
    void optimisticLockingConflict() {
        Book aliceCopy = books.findById(bookId).orElseThrow(); // two users load version 0
        Book bobCopy = books.findById(bookId).orElseThrow();
        System.out.println("both loaded version " + aliceCopy.getVersion() + " and " + bobCopy.getVersion());

        SqlCount sql = new SqlCount(emf);
        aliceCopy.setPrice(new BigDecimal("8.00"));
        sql.reset();
        books.save(aliceCopy);                                  // merge -> UPDATE ... WHERE id=? AND version=0
        sql.print("Alice saves");

        bobCopy.setPrice(new BigDecimal("12.00"));
        assertThatThrownBy(() -> books.save(bobCopy))           // still version 0 -> 0 rows updated
                .isInstanceOf(ObjectOptimisticLockingFailureException.class)
                .satisfies(e -> System.out.println("Bob saves -> " + e.getClass().getSimpleName() + ": "
                        + e.getMessage()));
        System.out.println("price in DB = " + books.findById(bookId).orElseThrow().getPrice() + " (Alice's)");
    }
}
