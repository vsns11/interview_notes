package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import java.util.List;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.Sort;

@DataJpaTest(showSql = false) // the slice turns on spring.jpa.show-sql by default; we print SQL ourselves
class RepositoryQueriesTest {

    @Autowired BookRepository books;
    @Autowired EntityManagerFactory emf;

    private static List<String> titles(List<Book> list) {
        return list.stream().map(Book::getTitle).toList();
    }

    @Test
    void derivedQueries() {
        System.out.println("containing 'go'      : " + titles(books.findByTitleContainingIgnoreCase("go")));
        System.out.println("price < 10 asc       : " + titles(books.findByPriceLessThanOrderByPriceAsc(new BigDecimal("10"))));
        System.out.println("by author name       : " + titles(books.findByAuthorName("Ursula K. Le Guin")));
        System.out.println("count by author      : " + books.countByAuthorName("Terry Pratchett"));
        System.out.println("exists 'Kindred'     : " + books.existsByTitle("Kindred"));
        System.out.println("top 2 by price       : " + titles(books.findTop2ByOrderByPriceDesc()));
        System.out.println("tag sci-fi, title asc: " + titles(books.findByTagsName("sci-fi", Sort.by("title"))));
        // Spring Data 2026.0+: type-safe property reference instead of the string "price"
        System.out.println("tag fantasy, by price: " + titles(books.findByTagsName("fantasy", Sort.by(Book::getPrice))));
        assertThat(books.countByAuthorName("Terry Pratchett")).isEqualTo(3);
        assertThat(titles(books.findTop2ByOrderByPriceDesc())).containsExactly("Kindred", "The Dispossessed");
    }

    @Test
    void sqlOfSomeDerivedQueries() {
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        books.findByTitleContainingIgnoreCase("go");
        books.countByAuthorName("Terry Pratchett");
        books.existsByTitle("Kindred");
        sql.print("containing / count / exists");
        assertThat(sql.statements()).isEqualTo(3);
    }

    @Test
    void pagingAndSorting() {
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        Page<Book> page = books.findByPriceGreaterThan(new BigDecimal("8"),
                PageRequest.of(1, 2, Sort.by("price").descending()));
        System.out.println("Page " + page.getNumber() + " of " + page.getTotalPages() + " (" + page.getTotalElements()
                + " rows total): " + titles(page.getContent()));
        Slice<Book> slice = books.findByAuthorIdIn(List.of(1L, 2L), PageRequest.of(0, 3, Sort.by("id")));
        System.out.println("Slice 0: " + titles(slice.getContent()) + ", hasNext=" + slice.hasNext());
        sql.print("Page + Slice");
        assertThat(page.getTotalElements()).isEqualTo(6);
        assertThat(slice.hasNext()).isTrue();
    }

    @Test
    void jpqlNativeAndModifyingQueries() {
        System.out.println("JPQL  Pratchett <= 11 : " + titles(books.byAuthorUpTo("Terry Pratchett", new BigDecimal("11"))));
        System.out.println("native titles > 11    : " + books.titlesAbove(new BigDecimal("11")));
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        int changed = books.reprice(4L, new BigDecimal("2"));
        sql.print("@Modifying bulk update");
        System.out.println("bulk update rows      : " + changed + " -> Asimov now "
                + books.findByAuthorId(4L, BookRow.class));
        assertThat(changed).isEqualTo(2);
    }

    @Test
    void projections() {
        List<BookSummary> summaries = books.findByAuthorNameOrderByTitle("Terry Pratchett");
        summaries.forEach(s -> System.out.println("interface projection: " + s.getTitle() + " " + s.getPrice()));
        System.out.println("record projection     : " + books.findByPriceLessThan(new BigDecimal("8")));
        System.out.println("constructor expression: " + books.rowsForAuthor(1L));
        System.out.println("dynamic (BookSummary) : " + books.findByAuthorId(3L, BookSummary.class).getFirst().getTitle());
        assertThat(books.findByPriceLessThan(new BigDecimal("8")))
                .containsExactlyInAnyOrder(new BookRow("Foundation", new BigDecimal("7.99")),
                                           new BookRow("I, Robot", new BigDecimal("6.99")));
    }

    @Test
    void projectionSelectsOnlyNeededColumns() {
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        books.findByAuthorNameOrderByTitle("Terry Pratchett");
        sql.print("interface projection SQL");
        assertThat(sql.sql().getFirst()).doesNotContain("version");
    }

    @Test
    void pessimisticLock() {
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        books.findWithLockById(1L);
        sql.print("@Lock(PESSIMISTIC_WRITE)");
        assertThat(sql.sql().getFirst()).endsWith("for update");
    }
}
