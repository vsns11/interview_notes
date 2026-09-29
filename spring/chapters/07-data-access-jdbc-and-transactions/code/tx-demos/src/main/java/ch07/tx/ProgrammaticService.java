package ch07.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.support.TransactionTemplate;

/** Programmatic transactions: no proxy involved, so self-invocation is not an issue. */
@Service
public class ProgrammaticService {

    private final Db db;
    private final TransactionTemplate tx;          // auto-configured by Spring Boot
    private final TransactionTemplate serializable;

    public ProgrammaticService(Db db, TransactionTemplate tx, PlatformTransactionManager txManager) {
        this.db = db;
        this.tx = tx;
        this.serializable = new TransactionTemplate(txManager);
        this.serializable.setIsolationLevel(TransactionDefinition.ISOLATION_SERIALIZABLE);
        this.serializable.setTimeout(5);
    }

    /** Only the lines inside the callback are transactional. */
    public long transferAndReturnNewBalance(String from, String to, long amount) {
        return tx.execute(status -> {
            db.addToBalance(to, amount);
            db.addToBalance(from, -amount);
            return db.balance(to);
        });
    }

    /** Roll back without throwing: mark the status rollback-only. */
    public String tryThenUndo() {
        return tx.execute(status -> {
            db.audit("temporary");
            String seen = "inside: " + db.auditMessages();
            status.setRollbackOnly();
            return seen;
        });
    }

    public String isolationOfCustomTemplate() {
        return serializable.execute(status -> db.showIsolation());
    }
}
