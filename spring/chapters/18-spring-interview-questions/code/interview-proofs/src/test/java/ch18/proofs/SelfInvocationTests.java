package ch18.proofs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Q: "Why does @Transactional do nothing when I call the method from the same class?" */
class SelfInvocationTests {

    static class ImportService {
        private final JdbcClient jdbc;
        ImportService(JdbcClient jdbc) { this.jdbc = jdbc; }

        public void importAll() {                // NOT transactional
            saveBatch();                         // this.saveBatch() -> skips the proxy
        }

        @Transactional
        public void saveBatch() {
            System.out.println("  inside saveBatch, transaction active? "
                    + TransactionSynchronizationManager.isActualTransactionActive());
            jdbc.sql("INSERT INTO orders VALUES (10, 'a')").update();
            throw new IllegalStateException("second insert failed");
        }
    }

    @Import({TxTestConfig.class, ImportService.class}) static class Config { }

    @Test
    void internalCallBypassesTheProxy() {
        new ApplicationContextRunner().withUserConfiguration(Config.class).run(ctx -> {
            ImportService svc = ctx.getBean(ImportService.class);
            JdbcClient jdbc = ctx.getBean(JdbcClient.class);
            System.out.println("bean class: " + svc.getClass().getSimpleName());

            System.out.println("call through the proxy (svc.saveBatch()):");
            assertThatThrownBy(svc::saveBatch).isInstanceOf(IllegalStateException.class);
            long afterProxyCall = jdbc.sql("SELECT COUNT(*) FROM orders").query(Long.class).single();
            System.out.println("  rows after failure: " + afterProxyCall + "  (rolled back)");

            System.out.println("self-invocation (svc.importAll() -> this.saveBatch()):");
            assertThatThrownBy(svc::importAll).isInstanceOf(IllegalStateException.class);
            long afterSelfCall = jdbc.sql("SELECT COUNT(*) FROM orders").query(Long.class).single();
            System.out.println("  rows after failure: " + afterSelfCall + "  (NOT rolled back - no transaction)");

            assertThat(afterProxyCall).isZero();
            assertThat(afterSelfCall).isEqualTo(1);
        });
    }
}
