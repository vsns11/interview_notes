package ch07.tx;

import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * NOT @Transactional on purpose: these demos must really commit and roll back.
 * All subclasses share one cached application context.
 */
@SpringBootTest
abstract class PgTestBase {

    @Autowired protected Db db;

    @BeforeEach
    void resetData() {
        db.reset(); // alice = 100, bob = 50, empty audit_log
    }

    protected String balances() {
        return "alice=" + db.balance("alice") + " bob=" + db.balance("bob");
    }
}
