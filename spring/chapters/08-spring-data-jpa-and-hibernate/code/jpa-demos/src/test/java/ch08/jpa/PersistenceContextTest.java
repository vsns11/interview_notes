package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.boot.jpa.test.autoconfigure.TestEntityManager;

@DataJpaTest(showSql = false) // the slice turns on spring.jpa.show-sql by default; we print SQL ourselves
class PersistenceContextTest {

    @Autowired TestEntityManager em;     // thin wrapper over the EntityManager of the test transaction
    @Autowired BookRepository books;
    @Autowired AuthorRepository authors;
    @Autowired EntityManagerFactory emf;
    SqlCount sql;

    @BeforeEach
    void startCounting() {
        sql = new SqlCount(emf);
        sql.reset();
    }

    @Test
    void firstLevelCacheGivesTheSameInstance() {
        Book first = books.findById(1L).orElseThrow();
        Book second = books.findById(1L).orElseThrow();
        System.out.println("same Java object? " + (first == second));
        sql.print("findById(1) twice");
        assertThat(first).isSameAs(second);
        assertThat(sql.statements()).isEqualTo(1);
    }

    @Test
    void dirtyCheckingWritesChangesWithoutSave() {
        Book book = books.findById(1L).orElseThrow();
        book.setPrice(new BigDecimal("14.99"));   // just change the Java object...
        em.flush();                                // ...Hibernate compares with its snapshot and writes SQL
        sql.print("setPrice() + flush, no save()");
        assertThat(sql.sql()).last().asString().startsWith("update book");
        System.out.println("version is now " + book.getVersion());
    }

    @Test
    void detachedEntitiesAreNotTracked() {
        Book book = books.findById(1L).orElseThrow();
        em.clear();                                // persistence context emptied: book is DETACHED
        book.setPrice(new BigDecimal("1.00"));
        em.flush();
        sql.print("change a detached entity + flush");
        assertThat(sql.statements()).isEqualTo(1); // only the original SELECT
    }

    @Test
    void cascadePersistAndIdentityInserts() {
        Author author = new Author("N. K. Jemisin");
        author.addBook(new Book("The Fifth Season", new BigDecimal("11.00")));
        author.addBook(new Book("The Obelisk Gate", new BigDecimal("11.00")));
        authors.save(author);                      // persist cascades to both books
        sql.print("save(new author with 2 new books)");
        System.out.println("ids assigned: author " + author.getId() + ", books "
                + author.getBooks().stream().map(Book::getId).toList());
        assertThat(sql.sql()).hasSize(3).allMatch(s -> s.startsWith("insert"));
    }

    @Test
    void orphanRemovalDeletesTheChild() {
        Author pratchett = authors.findById(2L).orElseThrow();
        Book smallGods = pratchett.getBooks().stream()
                .filter(b -> b.getTitle().equals("Small Gods")).findFirst().orElseThrow();
        pratchett.removeBook(smallGods);           // no repository.delete() call
        em.flush();
        sql.print("remove 'Small Gods' from author.books + flush");
        assertThat(sql.sql()).anyMatch(s -> s.startsWith("delete from book "));
    }
}
