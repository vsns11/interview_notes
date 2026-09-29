package ch07.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** Simulates "another user": REQUIRES_NEW = a second connection that commits on its own. */
@Service
public class ConcurrentWriter {

    private final Db db;

    public ConcurrentWriter(Db db) {
        this.db = db;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void deposit(String owner, long amount) {
        db.addToBalance(owner, amount);
    }
}
