package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;

@DataJpaTest(showSql = false) // JPA slice + embedded H2; each test runs in a transaction that is rolled back
class NPlusOneTest {

    @Autowired AuthorRepository authors;
    @Autowired ReviewRepository reviews;
    @Autowired EntityManagerFactory emf;
    SqlCount sql;

    @BeforeEach
    void startCounting() {
        sql = new SqlCount(emf);
        sql.reset();
    }

    private static void show(List<Author> list) {
        list.forEach(a -> System.out.println("  " + a.getName() + ": " + a.getBooks().size() + " books"));
    }

    @Test
    void theNPlusOneProblem() {
        List<Author> all = authors.findAll();
        show(all);                               // each getBooks().size() fires one more SELECT
        sql.print("findAll() then a.getBooks().size()");
        assertThat(sql.statements()).isEqualTo(1 + all.size());
    }

    @Test
    void fixWithJoinFetch() {
        show(authors.findAllWithBooks());
        sql.print("JOIN FETCH");
        assertThat(sql.statements()).isEqualTo(1);
    }

    @Test
    void fixWithEntityGraph() {
        show(authors.findAllWithBooksGraph());
        sql.print("@EntityGraph(attributePaths = \"books\")");
        assertThat(sql.statements()).isEqualTo(1);
    }

    @Test
    void eagerManyToOneIsAlsoNPlusOne() {
        var all = reviews.findAll();             // we never touch review.getBook()
        sql.print("reviews.findAll() with EAGER @ManyToOne Review.book (" + all.size() + " reviews)");
        assertThat(sql.statements()).isEqualTo(1 + 5); // 1 for reviews + 1 per distinct book
    }

    @Test
    void pagingAJoinFetch() {
        var page = authors.pageWithBooks(PageRequest.of(0, 2, Sort.by("id")));
        show(page.getContent());
        sql.print("JOIN FETCH + Pageable(page 0, size 2)", 1000);
        assertThat(page.getContent()).hasSize(2);
    }
}
