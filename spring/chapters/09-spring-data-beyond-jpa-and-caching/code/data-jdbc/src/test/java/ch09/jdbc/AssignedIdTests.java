package ch09.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.data.jdbc.core.JdbcAggregateTemplate;

@DataJdbcTest
class AssignedIdTests {

    @Autowired ProductRepository products;
    @Autowired JdbcAggregateTemplate template;

    @Test
    void saveWithAssignedIdSilentlyUpdatesNothing() {
        Product mug = new Product("MUG-3", "Coffee mug", new BigDecimal("9.00"));

        // id is not null and there is no @Version -> Spring Data thinks the row exists -> UPDATE
        SqlLog sql = new SqlLog();
        products.save(mug);                                        // no exception...
        sql.print("save(new Product(\"MUG-3\", ...))");
        System.out.println("rows after save():   " + products.count() + "   <- nothing was inserted!");
        assertThat(products.existsById("MUG-3")).isFalse();

        template.insert(mug);                                      // say explicitly: this is new
        System.out.println("rows after insert(): " + products.count() + "   " + products.findById("MUG-3").orElseThrow());
        assertThat(products.count()).isEqualTo(3);                 // 2 seeded by Flyway + the mug
    }
}
