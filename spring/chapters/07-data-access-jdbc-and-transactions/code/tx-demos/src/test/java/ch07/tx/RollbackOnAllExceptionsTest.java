package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ActiveProfiles;

/** Separate context: @EnableTransactionManagement(rollbackOn = ALL_EXCEPTIONS) is switched on. */
@ActiveProfiles("rollback-all")
class RollbackOnAllExceptionsTest extends PgTestBase {

    @Autowired TransferService transfers;

    @Test
    void checkedExceptionNowRollsBackWithoutRollbackFor() {
        assertThatThrownBy(() -> transfers.transferThenFailChecked("alice", "bob", 30))
                .isInstanceOf(FraudCheckException.class);
        System.out.println("rollbackOn=ALL_EXCEPTIONS, checked exception -> rolled back, " + balances());
        assertThat(balances()).isEqualTo("alice=100 bob=50");
    }
}
