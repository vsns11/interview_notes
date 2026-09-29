package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.logging.LogLevel;
import org.springframework.boot.logging.LoggingSystem;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.UnexpectedRollbackException;

class PropagationTest extends PgTestBase {

    private static final String TX_LOGGER = "org.springframework.jdbc.support.JdbcTransactionManager";

    @Autowired OuterService outer;
    @Autowired AuditService audit;
    @Autowired LoggingSystem logging;

    @BeforeEach
    void showTransactionManagerLog() {
        logging.setLogLevel(TX_LOGGER, LogLevel.DEBUG); // prints "Creating new transaction ...", "Suspending ..."
    }

    @AfterEach
    void hideTransactionManagerLog() {
        logging.setLogLevel(TX_LOGGER, LogLevel.WARN);
    }

    @Test
    void eachPropagationInsideAnOuterTransaction() {
        System.out.println("=== one outer transaction calling each propagation ===");
        List<String> seen = outer.callEachPropagation();
        seen.forEach(System.out::println);
        assertThat(seen.get(1)).endsWith(seen.get(0).substring(seen.get(0).indexOf("pid=")));      // same connection
        assertThat(seen.get(2)).doesNotEndWith(seen.get(0).substring(seen.get(0).indexOf("pid="))); // new connection
        assertThat(seen.get(6)).startsWith("NOT_SUPPORTED: txActive=false");
    }

    @Test
    void withoutAnOuterTransaction() {
        System.out.println("=== called with no transaction active ===");
        System.out.println("SUPPORTS      : " + audit.supports());
        assertThatThrownBy(() -> audit.mandatory("x"))
                .isInstanceOf(IllegalTransactionStateException.class)
                .satisfies(e -> System.out.println("MANDATORY     : " + e.getMessage()));
        assertThatThrownBy(() -> outer.outerCallsNever())
                .isInstanceOf(IllegalTransactionStateException.class)
                .satisfies(e -> System.out.println("NEVER in tx   : " + e.getMessage()));
    }

    @Test
    void requiresNewSurvivesOuterRollback() {
        System.out.println("=== REQUIRES_NEW commits even though the outer transaction rolls back ===");
        assertThatThrownBy(() -> outer.outerFailsAfterRequiresNew()).isInstanceOf(IllegalStateException.class);
        System.out.println("audit_log = " + db.auditMessages());
        assertThat(db.auditMessages()).containsExactly("audit written in REQUIRES_NEW");
    }

    @Test
    void nestedRollsBackToSavepointOnly() {
        System.out.println("=== NESTED failure undoes only the nested part ===");
        outer.outerSurvivesNestedFailure();
        System.out.println("audit_log = " + db.auditMessages());
        assertThat(db.auditMessages()).containsExactly("outer work", "outer caught: inner NESTED failed");
    }

    @Test
    void catchingAnInnerRequiredFailureDoesNotSaveYou() {
        System.out.println("=== inner REQUIRED failed, outer caught it and tried to commit ===");
        assertThatThrownBy(() -> outer.outerCatchesRequiredFailure())
                .isInstanceOf(UnexpectedRollbackException.class)
                .satisfies(e -> System.out.println(e.getClass().getSimpleName() + ": " + e.getMessage()));
        System.out.println("audit_log = " + db.auditMessages());
        assertThat(db.auditMessages()).isEmpty();
    }
}
