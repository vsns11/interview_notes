package ch09.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import javax.sql.DataSource;
import org.flywaydb.core.Flyway;
import org.flywaydb.core.api.MigrationInfo;
import org.flywaydb.core.api.output.ValidateResult;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;

@DataJdbcTest
class FlywayTests {

    @Autowired Flyway flyway;
    @Autowired DataSource dataSource;
    @Autowired CustomerRepository customers;

    @Test
    void sqlJavaAndRepeatableMigrationsAllRan() {
        for (MigrationInfo m : flyway.info().all()) {
            System.out.printf("%-4s %-22s %-5s %s%n",
                    m.getVersion() == null ? "R" : "V" + m.getVersion(), m.getDescription(), m.getType(), m.getState());
        }
        customers.findAll().forEach(c -> System.out.println("customer: " + c));
        assertThat(customers.findAll()).extracting(Customer::email).allMatch(e -> e.equals(e.toLowerCase()));
    }

    @Test
    void editingAnAppliedMigrationIsDetected() {
        Flyway tampered = Flyway.configure()
                .dataSource(dataSource)
                .locations("classpath:db/tampered")                 // same files, but V1 was edited
                .load();
        ValidateResult result = tampered.validateWithResult();
        System.out.println("validation successful: " + result.validationSuccessful);
        result.invalidMigrations.forEach(e -> System.out.println("  " + e.version + ": " + e.errorDetails.errorMessage));
        assertThat(result.validationSuccessful).isFalse();
    }
}
