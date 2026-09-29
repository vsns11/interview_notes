package ch18.proofs;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.annotation.Transactional;

/** Q: "My method threw an exception — why was the row still committed?" */
class RollbackTests {

    static class PaymentDeclinedException extends Exception {        // CHECKED
        PaymentDeclinedException(String msg) { super(msg); }
    }

    static class OrderService {
        private final JdbcClient jdbc;
        OrderService(JdbcClient jdbc) { this.jdbc = jdbc; }

        private void insert(int id) {
            jdbc.sql("INSERT INTO orders VALUES (?, 'book')").param(id).update();
        }

        @Transactional                                     // default rules
        public void checkedDefault(int id) throws PaymentDeclinedException {
            insert(id);
            throw new PaymentDeclinedException("card declined");
        }

        @Transactional(rollbackFor = PaymentDeclinedException.class)
        public void checkedWithRollbackFor(int id) throws PaymentDeclinedException {
            insert(id);
            throw new PaymentDeclinedException("card declined");
        }

        @Transactional
        public void unchecked(int id) {
            insert(id);
            throw new IllegalStateException("bug");
        }
    }

    @Import({TxTestConfig.class, OrderService.class}) static class Config { }

    @Test
    void checkedExceptionCommitsUnlessToldOtherwise() {
        new ApplicationContextRunner().withUserConfiguration(Config.class).run(ctx -> {
            OrderService svc = ctx.getBean(OrderService.class);
            JdbcClient jdbc = ctx.getBean(JdbcClient.class);

            assertThatThrownBy(() -> svc.checkedDefault(1)).isInstanceOf(PaymentDeclinedException.class);
            assertThatThrownBy(() -> svc.checkedWithRollbackFor(2)).isInstanceOf(PaymentDeclinedException.class);
            assertThatThrownBy(() -> svc.unchecked(3)).isInstanceOf(IllegalStateException.class);

            var ids = jdbc.sql("SELECT id FROM orders ORDER BY id").query(Integer.class).list();
            System.out.println("checked exception, default rules  -> row 1 kept?  " + ids.contains(1));
            System.out.println("checked exception, rollbackFor    -> row 2 kept?  " + ids.contains(2));
            System.out.println("RuntimeException, default rules   -> row 3 kept?  " + ids.contains(3));
            assertThat(ids).containsExactly(1);
        });
    }
}
