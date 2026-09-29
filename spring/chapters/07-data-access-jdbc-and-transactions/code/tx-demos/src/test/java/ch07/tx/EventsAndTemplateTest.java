package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

class EventsAndTemplateTest extends PgTestBase {

    @Autowired OrderService orders;
    @Autowired OrderListeners listeners;
    @Autowired ProgrammaticService programmatic;

    @BeforeEach
    void clearCalls() {
        listeners.calls.clear();
    }

    @Test
    void committedTransaction() {
        orders.place("book", false);
        System.out.println("commit   -> " + listeners.calls);
        assertThat(listeners.calls).containsExactly(
                "@EventListener book", "BEFORE_COMMIT book", "AFTER_COMMIT book", "AFTER_COMPLETION book");
    }

    @Test
    void rolledBackTransaction() {
        assertThatThrownBy(() -> orders.place("lamp", true));
        System.out.println("rollback -> " + listeners.calls + ", audit_log=" + db.auditMessages());
        assertThat(listeners.calls).containsExactly(
                "@EventListener lamp", "AFTER_ROLLBACK lamp", "AFTER_COMPLETION lamp");
    }

    @Test
    void noTransaction() {
        orders.placeWithoutTransaction("pen");
        System.out.println("no tx    -> " + listeners.calls);
        assertThat(listeners.calls).containsExactly("@EventListener pen");
    }

    @Test
    void transactionTemplate() {
        System.out.println("TransactionTemplate transfer -> bob now " + programmatic.transferAndReturnNewBalance("alice", "bob", 20));
        System.out.println("setRollbackOnly: " + programmatic.tryThenUndo() + ", after: " + db.auditMessages());
        System.out.println("custom template isolation = " + programmatic.isolationOfCustomTemplate());
        assertThat(balances()).isEqualTo("alice=80 bob=70");
        assertThat(db.auditMessages()).isEmpty();
    }
}
