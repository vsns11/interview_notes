package ch07.tx;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TransferService {

    private final Db db;

    public TransferService(Db db) {
        this.db = db;
    }

    /** Credit first, then debit: if the debit breaks CHECK (balance >= 0) the credit must be undone too. */
    @Transactional
    public void transfer(String from, String to, long amount) {
        db.addToBalance(to, amount);
        db.addToBalance(from, -amount);
    }

    @Transactional
    public void transferThenFailUnchecked(String from, String to, long amount) {
        transferSteps(from, to, amount);
        throw new IllegalStateException("fraud check failed (unchecked)");
    }

    @Transactional
    public void transferThenFailChecked(String from, String to, long amount) throws FraudCheckException {
        transferSteps(from, to, amount);
        throw new FraudCheckException("fraud check failed (checked)");
    }

    @Transactional(rollbackFor = FraudCheckException.class)
    public void transferThenFailCheckedWithRollbackFor(String from, String to, long amount)
            throws FraudCheckException {
        transferSteps(from, to, amount);
        throw new FraudCheckException("fraud check failed (checked, rollbackFor)");
    }

    @Transactional(noRollbackFor = IllegalArgumentException.class)
    public void transferWithNoRollbackFor(String from, String to, long amount) {
        transferSteps(from, to, amount);
        throw new IllegalArgumentException("warning only - keep the transfer");
    }

    private void transferSteps(String from, String to, long amount) {
        db.addToBalance(to, amount);
        db.addToBalance(from, -amount);
    }
}
