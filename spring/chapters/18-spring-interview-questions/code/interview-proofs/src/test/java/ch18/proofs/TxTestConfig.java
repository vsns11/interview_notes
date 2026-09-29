package ch18.proofs;

import javax.sql.DataSource;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.jdbc.datasource.DataSourceTransactionManager;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.annotation.EnableTransactionManagement;

/** Plain Spring wiring for the transaction proofs: in-memory H2 + a transaction manager. */
@Configuration(proxyBeanMethods = false)
@EnableTransactionManagement
class TxTestConfig {
    @Bean DataSource dataSource() {
        return new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).generateUniqueName(true)
                .addScript("classpath:schema.sql").build();
    }
    @Bean PlatformTransactionManager txManager(DataSource ds) { return new DataSourceTransactionManager(ds); }
    @Bean JdbcClient jdbc(DataSource ds) { return JdbcClient.create(ds); }
}
