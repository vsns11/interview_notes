package ch19.checks;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.transaction.IllegalTransactionStateException;
import org.springframework.transaction.UnexpectedRollbackException;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronizationManager;

/** Sheet 19.6: @Transactional defaults, the seven propagation types and the rollback rules. */
class TransactionTests {

    static class PaymentDeclined extends Exception {                    // a CHECKED exception
        PaymentDeclined(String m) { super(m); }
    }

    /** The inner bean: every call from Outer goes through its proxy. */
    static class Inner {
        private final JdbcClient jdbc;
        Inner(JdbcClient jdbc) { this.jdbc = jdbc; }
        void insert(String m) { jdbc.sql("INSERT INTO audit VALUES (?)").param(m).update(); }

        @Transactional String required() { return TransactionSynchronizationManager.getCurrentTransactionName(); }
        @Transactional(propagation = Propagation.REQUIRES_NEW) void requiresNew(String m) { insert(m); }
        @Transactional(propagation = Propagation.NESTED) void nestedThenFail(String m) { insert(m); throw new IllegalStateException("nested failed"); }
        @Transactional(propagation = Propagation.MANDATORY) void mandatory() { }
        @Transactional(propagation = Propagation.NEVER) void never() { }
        @Transactional(propagation = Propagation.SUPPORTS) boolean supports() { return TransactionSynchronizationManager.isActualTransactionActive(); }
        @Transactional(propagation = Propagation.NOT_SUPPORTED) boolean notSupported() { return TransactionSynchronizationManager.isActualTransactionActive(); }
        @Transactional void requiredThenFail() { throw new IllegalStateException("inner REQUIRED failed"); }

        @Transactional void checkedDefault(String m) throws PaymentDeclined { insert(m); throw new PaymentDeclined("declined"); }
        @Transactional(rollbackFor = PaymentDeclined.class) void checkedRollbackFor(String m) throws PaymentDeclined { insert(m); throw new PaymentDeclined("declined"); }
        @Transactional void unchecked(String m) { insert(m); throw new IllegalStateException("bug"); }
        @Transactional void caughtInside(String m) { insert(m); try { throw new IllegalStateException("x"); } catch (IllegalStateException ignored) { } }
    }

    static class Outer {
        private final Inner inner;
        Outer(Inner inner) { this.inner = inner; }

        @Transactional String[] requiredJoins() {
            return new String[] {TransactionSynchronizationManager.getCurrentTransactionName(), inner.required()};
        }
        @Transactional void requiresNewSurvives() { inner.insert("outer"); inner.requiresNew("audit (REQUIRES_NEW)"); throw new IllegalStateException("outer fails"); }
        @Transactional void nestedPartial() {
            inner.insert("outer");
            try { inner.nestedThenFail("nested"); } catch (IllegalStateException expected) { }
        }
        @Transactional void callNever() { inner.never(); }
        @Transactional boolean[] supportsAndNotSupported() { return new boolean[] {inner.supports(), inner.notSupported()}; }
        @Transactional void catchInnerRequired() {
            inner.insert("outer");
            try { inner.requiredThenFail(); } catch (IllegalStateException swallowed) { }
        }
    }

    @Import({TxTestConfig.class, Inner.class, Outer.class}) static class Config { }

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(Config.class);

    private static List<String> rows(JdbcClient jdbc) {
        List<String> rows = jdbc.sql("SELECT msg FROM audit").query(String.class).list();
        jdbc.sql("DELETE FROM audit").update();
        return rows;
    }

    @Test
    void defaults() throws Exception {
        var m = Transactional.class;
        Object propagation = m.getMethod("propagation").getDefaultValue();
        Object isolation = m.getMethod("isolation").getDefaultValue();
        Object readOnly = m.getMethod("readOnly").getDefaultValue();
        Object timeout = m.getMethod("timeout").getDefaultValue();
        System.out.println("@Transactional defaults: propagation=" + propagation + " isolation=" + isolation
                + " readOnly=" + readOnly + " timeout=" + timeout);
        assertThat(propagation).isEqualTo(Propagation.REQUIRED);
        assertThat(isolation).isEqualTo(Isolation.DEFAULT);
        assertThat(readOnly).isEqualTo(false);
        assertThat(timeout).isEqualTo(-1);
    }

    @Test
    void propagation() {
        runner.run(ctx -> {
            Outer outer = ctx.getBean(Outer.class);
            Inner inner = ctx.getBean(Inner.class);
            JdbcClient jdbc = ctx.getBean(JdbcClient.class);

            String[] names = outer.requiredJoins();
            System.out.println("REQUIRED      : inner runs in the outer transaction? " + names[0].equals(names[1])
                    + " (" + names[1].substring(names[1].lastIndexOf('$') + 1) + ")");
            assertThat(names[1]).isEqualTo(names[0]);

            assertThatThrownBy(outer::requiresNewSurvives).hasMessage("outer fails");
            List<String> afterRequiresNew = rows(jdbc);
            System.out.println("REQUIRES_NEW  : outer rolled back, rows left = " + afterRequiresNew);
            assertThat(afterRequiresNew).containsExactly("audit (REQUIRES_NEW)");

            outer.nestedPartial();
            List<String> afterNested = rows(jdbc);
            System.out.println("NESTED        : inner failed + caught, rows left = " + afterNested + " (savepoint)");
            assertThat(afterNested).containsExactly("outer");

            assertThatThrownBy(inner::mandatory).isInstanceOf(IllegalTransactionStateException.class);
            assertThatThrownBy(outer::callNever).isInstanceOf(IllegalTransactionStateException.class);
            System.out.println("MANDATORY     : no transaction -> IllegalTransactionStateException");
            System.out.println("NEVER         : inside a transaction -> IllegalTransactionStateException");

            boolean supportsAlone = inner.supports();
            boolean[] inside = outer.supportsAndNotSupported();
            System.out.println("SUPPORTS      : active alone? " + supportsAlone + ", active inside a transaction? " + inside[0]);
            System.out.println("NOT_SUPPORTED : active inside a transaction? " + inside[1] + " (outer suspended)");
            assertThat(supportsAlone).isFalse();
            assertThat(inside[0]).isTrue();
            assertThat(inside[1]).isFalse();
        });
    }

    @Test
    void rollbackRules() {
        runner.run(ctx -> {
            Inner inner = ctx.getBean(Inner.class);
            Outer outer = ctx.getBean(Outer.class);
            JdbcClient jdbc = ctx.getBean(JdbcClient.class);

            assertThatThrownBy(() -> inner.checkedDefault("checked, default rules")).isInstanceOf(PaymentDeclined.class);
            assertThatThrownBy(() -> inner.checkedRollbackFor("checked, rollbackFor")).isInstanceOf(PaymentDeclined.class);
            assertThatThrownBy(() -> inner.unchecked("RuntimeException")).isInstanceOf(IllegalStateException.class);
            inner.caughtInside("caught inside the method");
            List<String> committed = rows(jdbc);
            System.out.println("committed despite an exception: " + committed);
            assertThat(committed).containsExactly("checked, default rules", "caught inside the method");

            assertThatThrownBy(outer::catchInnerRequired).isInstanceOf(UnexpectedRollbackException.class);
            List<String> afterCatch = rows(jdbc);
            System.out.println("inner REQUIRED failed, outer caught it -> UnexpectedRollbackException, rows = " + afterCatch);
            assertThat(afterCatch).isEmpty();
        });
    }
}
