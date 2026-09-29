package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class RollbackRulesTest extends PgTestBase {

    @Autowired TransferService transfers;

    @Test
    void databaseErrorRollsBackTheWholeTransfer() {
        assertThatThrownBy(() -> transfers.transfer("bob", "alice", 500))   // bob only has 50
                .isInstanceOf(DataIntegrityViolationException.class);
        System.out.println("CHECK violation      -> rolled back, " + balances());
        assertThat(balances()).isEqualTo("alice=100 bob=50");
    }

    @Test
    void uncheckedExceptionRollsBack() {
        assertThatThrownBy(() -> transfers.transferThenFailUnchecked("alice", "bob", 30));
        System.out.println("RuntimeException     -> rolled back, " + balances());
        assertThat(balances()).isEqualTo("alice=100 bob=50");
    }

    @Test
    void checkedExceptionCommits() {
        assertThatThrownBy(() -> transfers.transferThenFailChecked("alice", "bob", 30))
                .isInstanceOf(FraudCheckException.class);
        System.out.println("checked exception    -> COMMITTED (!), " + balances());
        assertThat(balances()).isEqualTo("alice=70 bob=80");
    }

    @Test
    void rollbackForMakesCheckedExceptionRollBack() {
        assertThatThrownBy(() -> transfers.transferThenFailCheckedWithRollbackFor("alice", "bob", 30))
                .isInstanceOf(FraudCheckException.class);
        System.out.println("checked + rollbackFor -> rolled back, " + balances());
        assertThat(balances()).isEqualTo("alice=100 bob=50");
    }

    @Test
    void noRollbackForKeepsTheWork() {
        assertThatThrownBy(() -> transfers.transferWithNoRollbackFor("alice", "bob", 30))
                .isInstanceOf(IllegalArgumentException.class);
        System.out.println("unchecked + noRollbackFor -> committed, " + balances());
        assertThat(balances()).isEqualTo("alice=70 bob=80");
    }
}
