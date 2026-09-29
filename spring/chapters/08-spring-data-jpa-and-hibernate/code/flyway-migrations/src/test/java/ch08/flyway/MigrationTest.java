package ch08.flyway;

import static org.assertj.core.api.Assertions.assertThat;

import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@DataJpaTest(showSql = false) // the JPA slice includes Flyway auto-configuration
class MigrationTest {

    @Autowired Flyway flyway;
    @Autowired CustomerRepository customers;
    @Autowired JdbcClient jdbc;

    @Test
    void migrationsRanInOrderAndHibernateValidatedTheSchema() {
        for (MigrationInfo m : flyway.info().all()) {
            System.out.printf("V%s  %-25s %s%n", m.getVersion(), m.getDescription(), m.getState());
        }
        jdbc.sql("SELECT \"installed_rank\", \"version\", \"description\", \"type\" FROM \"flyway_schema_history\" ORDER BY 1")
            .query((rs, n) -> "history: " + rs.getInt(1) + " | " + rs.getString(2) + " | " + rs.getString(3) + " | " + rs.getString(4))
            .list().forEach(System.out::println);
        customers.findAll().forEach(c -> System.out.println("customer: " + c.getName() + " <" + c.getEmail() + ">"));
        assertThat(flyway.info().current().getVersion().getVersion()).isEqualTo("3");
        assertThat(customers.count()).isEqualTo(2);
    }
}
