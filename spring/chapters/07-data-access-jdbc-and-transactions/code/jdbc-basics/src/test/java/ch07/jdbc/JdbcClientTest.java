package ch07.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import java.util.List;

import javax.sql.DataSource;

import com.zaxxer.hikari.HikariDataSource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.jdbc.test.autoconfigure.AutoConfigureTestDatabase;
import org.springframework.boot.jdbc.test.autoconfigure.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.transaction.support.TransactionSynchronizationManager;

// @JdbcTest = only JDBC beans + an embedded DB; every test runs in a transaction that is rolled back.
// Replace.NONE keeps OUR application.properties DataSource (Hikari + H2) instead of a generated one.
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({ProductRepository.class, ProductTemplateDao.class})
class JdbcClientTest {

    @Autowired DataSource dataSource;
    @Autowired ProductRepository repo;
    @Autowired ProductTemplateDao dao;

    @Test
    void dataSourceIsHikari() {
        HikariDataSource hikari = (HikariDataSource) dataSource;
        System.out.println("DataSource class = " + hikari.getClass().getSimpleName()
                + ", pool = " + hikari.getPoolName()
                + ", maxPoolSize = " + hikari.getMaximumPoolSize()
                + ", connectionTimeout = " + hikari.getConnectionTimeout() + " ms");
        assertThat(hikari.getMaximumPoolSize()).isEqualTo(5);
    }

    @Test
    void schemaAndDataSqlRanAtStartup() {
        List<Product> all = repo.findAll();
        all.forEach(p -> System.out.println("row: " + p));
        assertThat(all).extracting(Product::sku).containsExactly("KB-01", "MS-01", "MN-27");
    }

    @Test
    void queriesWithNamedAndPositionalParameters() {
        System.out.println("findBySku(MS-01) = " + repo.findBySku("MS-01"));
        System.out.println("findBySku(NOPE)  = " + repo.findBySku("NOPE"));
        System.out.println("cheaper than 100 = " + repo.namesCheaperThan(new BigDecimal("100")));
        assertThat(repo.findBySku("NOPE")).isEmpty();
        assertThat(repo.namesCheaperThan(new BigDecimal("100")))
                .containsExactly("Wireless mouse", "Mechanical keyboard");
    }

    @Test
    void insertReturnsGeneratedKeyAndUpdateReturnsRowCount() {
        long id = repo.insert("HD-01", "Headset", new BigDecimal("59.90"), 7);
        int changed = repo.changeStock("HD-01", -2);
        System.out.println("generated id = " + id + ", rows updated = " + changed
                + ", now = " + repo.findBySku("HD-01").orElseThrow());
        // > 3, not necessarily 4: identity/sequence values used by earlier (rolled-back) tests are never reused
        assertThat(id).isGreaterThan(3L);
        assertThat(changed).isEqualTo(1);
    }

    @Test
    void jdbcTemplateAndBatchUpdate() {
        int[] counts = dao.restock(List.of("KB-01", "MN-27", "NOPE"), 10);
        System.out.println("batch update counts = " + java.util.Arrays.toString(counts));
        System.out.println("in stock >= 13: " + dao.inStockAtLeast(13).stream().map(Product::sku).toList());
        assertThat(counts).containsExactly(1, 1, 0);
        assertThat(dao.count()).isEqualTo(3);
    }

    @Test
    void sqlExceptionsAreTranslatedToDataAccessExceptions() {
        assertThatThrownBy(() -> repo.insert("KB-01", "Duplicate", BigDecimal.ONE, 1))
                .isInstanceOf(DuplicateKeyException.class)
                .isInstanceOf(DataAccessException.class)   // unchecked, database-neutral hierarchy
                .satisfies(e -> System.out.println("duplicate sku -> " + e.getClass().getSimpleName()));
        assertThatThrownBy(() -> repo.insert("NEG-1", "Negative", new BigDecimal("-1"), 1))
                .isInstanceOf(DataIntegrityViolationException.class)
                .satisfies(e -> System.out.println("CHECK violated -> " + e.getClass().getSimpleName()));
    }

    @Test
    void eachTestRunsInsideARolledBackTransaction() {
        System.out.println("test method in a transaction? "
                + TransactionSynchronizationManager.isActualTransactionActive());
        repo.changeStock("KB-01", -12);          // undone after the test by the slice's rollback
        assertThat(TransactionSynchronizationManager.isActualTransactionActive()).isTrue();
    }
}
