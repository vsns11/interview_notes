package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.NestedExceptionUtils;
import org.springframework.dao.ConcurrencyFailureException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.QueryTimeoutException;

class IsolationTest extends PgTestBase {

    @Autowired IsolationService iso;

    @Test
    void readCommittedSeesOtherCommits() {
        var reads = iso.readTwiceReadCommitted();
        System.out.println("READ_COMMITTED  reads alice twice: " + reads + "  (non-repeatable read)");
        assertThat(reads).containsExactly(100L, 150L);
    }

    @Test
    void repeatableReadKeepsItsSnapshot() {
        var reads = iso.readTwiceRepeatableRead();
        System.out.println("REPEATABLE_READ reads alice twice: " + reads + "  (same snapshot)");
        assertThat(reads).containsExactly(100L, 100L);
    }

    @Test
    void lostUpdateUnderReadCommitted() {
        iso.addTenReadCommitted();
        System.out.println("READ_COMMITTED  read 100, other +50, write read+10 -> alice=" + db.balance("alice")
                + "  (the +50 is LOST)");
        assertThat(db.balance("alice")).isEqualTo(110L);
    }

    @Test
    void repeatableReadDetectsTheLostUpdate() {
        assertThatThrownBy(() -> iso.addTenRepeatableRead())
                .isInstanceOf(ConcurrencyFailureException.class)
                .satisfies(e -> System.out.println("REPEATABLE_READ same steps -> " + e.getClass().getSimpleName()
                        + ": " + NestedExceptionUtils.getMostSpecificCause(e).getMessage()));
        System.out.println("alice=" + db.balance("alice") + " (the deposit survived; retry the whole transaction)");
        assertThat(db.balance("alice")).isEqualTo(150L);
    }

    @Test
    void readOnlyTransactionRejectsWrites() {
        assertThatThrownBy(() -> iso.writeInReadOnly())
                .isInstanceOf(DataAccessException.class)
                .satisfies(e -> System.out.println("readOnly=true + INSERT -> " + e.getClass().getSimpleName()
                        + ": " + NestedExceptionUtils.getMostSpecificCause(e).getMessage()));
    }

    @Test
    void timeoutCancelsTheStatement() {
        long start = System.nanoTime();
        assertThatThrownBy(() -> iso.slowQuery())
                .isInstanceOf(QueryTimeoutException.class)
                .satisfies(e -> System.out.println("timeout=1 + pg_sleep(3) -> " + e.getClass().getSimpleName()
                        + ": " + NestedExceptionUtils.getMostSpecificCause(e).getMessage()));
        long ms = (System.nanoTime() - start) / 1_000_000;
        System.out.println("gave up after ~" + (ms / 100) * 100 + " ms");
        assertThat(ms).isLessThan(2500);
    }
}
