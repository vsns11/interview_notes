package ch07.tx;

import java.util.List;

import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Plain (non-transactional) data access used by every demo. It joins whatever transaction is active. */
@Component
public class Db {

    private final JdbcClient jdbc;

    public Db(JdbcClient jdbc) {
        this.jdbc = jdbc;
    }

    public void reset() {
        jdbc.sql("TRUNCATE audit_log RESTART IDENTITY").update();
        jdbc.sql("DELETE FROM account").update();
        jdbc.sql("INSERT INTO account VALUES ('alice', 100), ('bob', 50)").update();
    }

    public void addToBalance(String owner, long delta) {
        jdbc.sql("UPDATE account SET balance = balance + ? WHERE owner = ?").params(delta, owner).update();
    }

    public void setBalance(String owner, long value) {
        jdbc.sql("UPDATE account SET balance = ? WHERE owner = ?").params(value, owner).update();
    }

    public long balance(String owner) {
        return jdbc.sql("SELECT balance FROM account WHERE owner = ?").param(owner).query(Long.class).single();
    }

    public void audit(String message) {
        jdbc.sql("INSERT INTO audit_log (message) VALUES (?)").param(message).update();
    }

    public List<String> auditMessages() {
        return jdbc.sql("SELECT message FROM audit_log ORDER BY id").query(String.class).list();
    }

    public String showIsolation() {
        return jdbc.sql("SHOW transaction_isolation").query(String.class).single();
    }

    public void sleepSeconds(int seconds) {
        jdbc.sql("SELECT pg_sleep(?)").param(seconds).query(Object.class).single();
    }

    /** Which PostgreSQL backend (= physical connection) runs this code, and is a Spring transaction active? */
    public String where() {
        int pid = jdbc.sql("SELECT pg_backend_pid()").query(Integer.class).single();
        return "txActive=" + TransactionSynchronizationManager.isActualTransactionActive()
                + " name=" + TransactionSynchronizationManager.getCurrentTransactionName()
                + " pid=" + pid;
    }
}
