package ch07.tx;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** The "outer" bean: starts a transaction and calls AuditService through its proxy. */
@Service
public class OuterService {

    private final Db db;
    private final AuditService audit;

    public OuterService(Db db, AuditService audit) {
        this.db = db;
        this.audit = audit;
    }

    @Transactional
    public List<String> callEachPropagation() {
        List<String> seen = new ArrayList<>();
        seen.add("outer        : " + db.where());
        seen.add("REQUIRED     : " + audit.required("required"));
        seen.add("REQUIRES_NEW : " + audit.requiresNew("requires-new"));
        seen.add("NESTED       : " + audit.nestedAndMaybeFail("nested", false));
        seen.add("MANDATORY    : " + audit.mandatory("mandatory"));
        seen.add("SUPPORTS     : " + audit.supports());
        seen.add("NOT_SUPPORTED: " + audit.notSupported());
        return seen;
    }

    @Transactional
    public void outerFailsAfterRequiresNew() {
        db.audit("outer work");
        audit.requiresNew("audit written in REQUIRES_NEW");
        throw new IllegalStateException("outer failed");
    }

    @Transactional
    public void outerSurvivesNestedFailure() {
        db.audit("outer work");
        try {
            audit.nestedAndMaybeFail("nested work", true);
        } catch (IllegalStateException e) {
            db.audit("outer caught: " + e.getMessage());
        }
    }

    @Transactional
    public void outerCatchesRequiredFailure() {
        db.audit("outer work");
        try {
            audit.requiredAndFail("inner work");
        } catch (IllegalStateException e) {
            db.audit("outer caught: " + e.getMessage() + " and carries on");
        }
    }

    @Transactional
    public String outerCallsNever() {
        return audit.never();
    }
}
