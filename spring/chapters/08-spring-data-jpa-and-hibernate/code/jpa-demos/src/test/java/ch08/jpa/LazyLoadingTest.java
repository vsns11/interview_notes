package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/** A full context and NO test transaction - like code running in a controller or a scheduled job. */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
class LazyLoadingTest {

    @Autowired AuthorRepository authors;
    @Autowired LibraryService library;

    @Test
    void lazyCollectionOutsideTheTransaction() {
        Author a = authors.findById(2L).orElseThrow(); // repository call = its own short transaction
        assertThatThrownBy(() -> a.getBooks().size())
                .isInstanceOf(LazyInitializationException.class)
                .satisfies(e -> System.out.println("LazyInitializationException: " + e.getMessage()));
    }

    @Test
    void fixesThatWork() {
        Author withGraph = authors.findWithBooksById(2L).orElseThrow();
        System.out.println("fetched with @EntityGraph -> " + withGraph.getBooks().size() + " books");
        System.out.println("inside @Transactional service -> " + library.summary(2L));
        assertThat(withGraph.getBooks()).hasSize(3);
    }
}
