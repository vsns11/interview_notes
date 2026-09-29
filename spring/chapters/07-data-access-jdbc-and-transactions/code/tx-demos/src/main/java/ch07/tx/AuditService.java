package ch07.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

/** The "inner" bean. Each method has a different propagation so we can watch how it joins the caller. */
@Service
public class AuditService {

    private final Db db;

    public AuditService(Db db) {
        this.db = db;
    }

    @Transactional // REQUIRED: join the caller's transaction, or start one
    public String required(String msg) {
        db.audit(msg);
        return db.where();
    }

    @Transactional
    public void requiredAndFail(String msg) {
        db.audit(msg);
        throw new IllegalStateException("inner REQUIRED failed");
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW) // suspend caller, run in a brand-new transaction
    public String requiresNew(String msg) {
        db.audit(msg);
        return db.where();
    }

    @Transactional(propagation = Propagation.NESTED) // savepoint inside the caller's transaction
    public String nestedAndMaybeFail(String msg, boolean fail) {
        db.audit(msg);
        if (fail) {
            throw new IllegalStateException("inner NESTED failed");
        }
        return db.where();
    }

    @Transactional(propagation = Propagation.MANDATORY) // must be called inside a transaction
    public String mandatory(String msg) {
        db.audit(msg);
        return db.where();
    }

    @Transactional(propagation = Propagation.SUPPORTS) // join if there is one, else run without
    public String supports() {
        return db.where();
    }

    @Transactional(propagation = Propagation.NOT_SUPPORTED) // suspend caller, run without a transaction
    public String notSupported() {
        return db.where();
    }

    @Transactional(propagation = Propagation.NEVER) // fail if a transaction exists
    public String never() {
        return db.where();
    }
}
