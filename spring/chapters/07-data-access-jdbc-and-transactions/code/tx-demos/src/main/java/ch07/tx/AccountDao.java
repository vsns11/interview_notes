package ch07.tx;

import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

/** A DAO whose methods are each transactional (like Spring Data repository methods are by default). */
@Repository
public class AccountDao {

    private final Db db;

    public AccountDao(Db db) {
        this.db = db;
    }

    @Transactional
    public void add(String owner, long delta) {
        db.addToBalance(owner, delta);
    }
}
