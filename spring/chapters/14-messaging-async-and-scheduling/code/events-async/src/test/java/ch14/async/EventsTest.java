package ch14.async;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.awaitility.Awaitility.await;

import java.time.Duration;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.simple.JdbcClient;

@SpringBootTest
class EventsTest {

    @Autowired Trace trace;
    @Autowired SignupService signups;
    @Autowired OrderService orders;
    @Autowired JdbcClient jdbc;

    @BeforeEach
    void reset() { trace.clear(); }

    @Test
    void syncVersusAsyncListeners() {
        signups.signUp("ana@example.com");
        System.out.println("--- right after signUp() returned ---");
        trace.lines().forEach(System.out::println);
        assertThat(trace.lines()).hasSize(3);                     // the async listener has not finished yet

        await().atMost(Duration.ofSeconds(2)).until(() -> trace.lines().size() == 4);
        System.out.println("--- 200 ms later ---");
        System.out.println(trace.lines().get(3));
        assertThat(trace.lines().get(1)).contains("sync listener").contains("on main");
        assertThat(trace.lines().get(3)).contains("async listener").contains("on task-");
    }

    @Test
    void transactionPhasesOnCommit() {
        orders.place("book", false);
        await().atMost(Duration.ofSeconds(2)).until(() -> trace.lines().size() == 5);
        System.out.println("--- commit ---");
        trace.lines().forEach(System.out::println);
        assertThat(trace.lines().subList(0, 4)).containsExactly(
                "service: event published inside the transaction",
                "BEFORE_COMMIT    book (row visible in tx: 1)",
                "AFTER_COMMIT     book on main",
                "AFTER_COMPLETION book");
        assertThat(trace.lines().get(4)).startsWith("async AFTER_COMMIT email for book on task-");
        assertThat(emails("book")).isEqualTo(1);
    }

    @Test
    void transactionPhasesOnRollback() throws InterruptedException {
        assertThatThrownBy(() -> orders.place("lamp", true)).hasMessage("payment declined");
        Thread.sleep(300);   // give a (wrong) async email the chance to appear - it must not
        System.out.println("--- rollback ---");
        trace.lines().forEach(System.out::println);
        assertThat(trace.lines()).containsExactly(
                "service: event published inside the transaction",
                "AFTER_ROLLBACK   lamp (rows now: 0)",
                "AFTER_COMPLETION lamp");
        assertThat(emails("lamp")).isZero();
    }

    private long emails(String item) {
        return jdbc.sql("SELECT count(*) FROM email_log WHERE message = ?")
                .param("confirmation for " + item).query(Long.class).single();
    }
}
