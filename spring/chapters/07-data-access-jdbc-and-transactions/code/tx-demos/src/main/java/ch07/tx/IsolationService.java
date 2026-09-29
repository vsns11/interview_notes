package ch07.tx;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

/** Isolation, read-only and timeout demos. Run against PostgreSQL. */
@Service
public class IsolationService {

    private final Db db;
    private final ConcurrentWriter other;

    public IsolationService(Db db, ConcurrentWriter other) {
        this.db = db;
        this.other = other;
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public List<Long> readTwiceReadCommitted() {
        long first = db.balance("alice");
        other.deposit("alice", 50);          // someone else commits in between
        long second = db.balance("alice");
        return List.of(first, second);
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public List<Long> readTwiceRepeatableRead() {
        long first = db.balance("alice");
        other.deposit("alice", 50);
        long second = db.balance("alice");
        return List.of(first, second);
    }

    /** Read-modify-write in Java: the classic lost update. */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public void addTenReadCommitted() {
        long read = db.balance("alice");
        other.deposit("alice", 50);          // concurrent deposit
        db.setBalance("alice", read + 10);   // overwrites it
    }

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void addTenRepeatableRead() {
        long read = db.balance("alice");
        other.deposit("alice", 50);
        db.setBalance("alice", read + 10);   // PostgreSQL refuses: row changed since our snapshot
    }

    @Transactional(readOnly = true)
    public void writeInReadOnly() {
        db.audit("should not be allowed");
    }

    @Transactional(timeout = 1) // seconds; JdbcTemplate turns the remaining time into a JDBC query timeout
    public void slowQuery() {
        db.sleepSeconds(3);
    }
}
