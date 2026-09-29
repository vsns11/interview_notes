package ch07.tx;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.aop.support.AopUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

class ProxyPitfallsTest extends PgTestBase {

    @Autowired ImportService imports;
    @Autowired NoTxTransferService noTxTransfers;
    @Autowired TransferService transfers;

    @Test
    void theBeanIsAProxy() {
        System.out.println("injected class = " + imports.getClass().getSimpleName()
                + ", CGLIB proxy? " + AopUtils.isCglibProxy(imports));
        assertThat(AopUtils.isCglibProxy(imports)).isTrue();
    }

    @Test
    void selfInvocationSkipsTheTransaction() {
        assertThatThrownBy(() -> imports.importAll(List.of("line-1", "line-2", "BAD")));
        System.out.println("via this.saveAll()  -> audit_log = " + db.auditMessages() + "   (partial import!)");
        assertThat(db.auditMessages()).containsExactly("line-1", "line-2");

        db.reset();
        assertThatThrownBy(() -> imports.saveAll(List.of("line-1", "line-2", "BAD")));
        System.out.println("via proxy.saveAll() -> audit_log = " + db.auditMessages() + "   (rolled back)");
        assertThat(db.auditMessages()).isEmpty();
    }

    @Test
    void finalMethodIsNotIntercepted() {
        assertThatThrownBy(() -> imports.finalMethod())
                .isInstanceOf(NullPointerException.class)
                .satisfies(e -> System.out.println("final @Transactional method -> " + e.getClass().getSimpleName()
                        + " (runs on the proxy, whose 'db' field is null)"));
    }

    @Test
    void packagePrivateMethodIsTransactional() {
        String where = imports.packagePrivate();
        System.out.println("package-private @Transactional -> " + where);
        assertThat(where).startsWith("txActive=true");
    }

    @Test
    void transactionOnTheWrongLayer() {
        assertThatThrownBy(() -> noTxTransfers.transfer("bob", "alice", 500))
                .isInstanceOf(DataIntegrityViolationException.class);
        System.out.println("tx per DAO call     -> " + balances() + "   (500 created from nothing)");
        assertThat(balances()).isEqualTo("alice=600 bob=50");

        db.reset();
        assertThatThrownBy(() -> transfers.transfer("bob", "alice", 500))
                .isInstanceOf(DataIntegrityViolationException.class);
        System.out.println("tx on service method -> " + balances());
        assertThat(balances()).isEqualTo("alice=100 bob=50");
    }
}
