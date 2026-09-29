package ch08.jpa;

import static org.assertj.core.api.Assertions.assertThat;

import jakarta.persistence.EntityManagerFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;

/** Fix 3: batch fetching. Same code as the N+1 test, one property different. */
@DataJpaTest(showSql = false, properties = "spring.jpa.properties.hibernate.default_batch_fetch_size=16")
class BatchFetchTest {

    @Autowired AuthorRepository authors;
    @Autowired EntityManagerFactory emf;

    @Test
    void lazyCollectionsAreLoadedInBatches() {
        SqlCount sql = new SqlCount(emf);
        sql.reset();
        authors.findAll().forEach(a -> System.out.println("  " + a.getName() + ": " + a.getBooks().size() + " books"));
        sql.print("findAll() + books with default_batch_fetch_size=16");
        assertThat(sql.statements()).isEqualTo(2);
    }
}
