package ch09.jdbc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jdbc.test.autoconfigure.DataJdbcTest;
import org.springframework.dao.OptimisticLockingFailureException;

@DataJdbcTest                                // JDBC + Spring Data JDBC + Flyway, each test rolled back
class AggregateTests {

    @Autowired PurchaseOrderRepository orders;
    final SqlLog sql = new SqlLog();

    PurchaseOrder newOrder() {
        var order = new PurchaseOrder(1L, new Address("1 Main St", "Pune"));
        order.addLine("BOOK-1", 1, new BigDecimal("40.00"));
        order.addLine("PEN-7", 4, new BigDecimal("2.50"));
        return order;
    }

    @Test
    void savingTheRootSavesItsLinesAndLoadingReadsThemBack() {
        sql.clear();
        PurchaseOrder saved = orders.save(newOrder());
        sql.print("save(new order with 2 lines)");

        sql.clear();
        PurchaseOrder loaded = orders.findById(saved.getId()).orElseThrow();
        sql.print("findById");
        System.out.println("loaded: id=" + loaded.getId() + " version=" + loaded.getVersion()
                + " customer=" + loaded.getCustomerId().getId() + " lines=" + loaded.getLines().size()
                + " total=" + loaded.total() + " shipTo=" + loaded.getShipTo()
                + " createdAt set=" + (loaded.getCreatedAt() != null));

        assertThat(loaded.getLines()).extracting(OrderLine::sku).containsExactly("BOOK-1", "PEN-7");
        assertThat(loaded.total()).isEqualByComparingTo("50.00");
        assertThat(loaded.getVersion()).isZero();
    }

    @Test
    void updatingTheRootDeletesAndReinsertsAllLines() {
        PurchaseOrder order = orders.save(newOrder());
        order.addLine("PEN-7", 1, new BigDecimal("2.50"));     // change ONE thing: add a third line

        sql.clear();
        orders.save(order);
        sql.print("save(order with 1 added line)");

        assertThat(sql.statements()).anyMatch(s -> s.startsWith("DELETE"));
        assertThat(order.getVersion()).isEqualTo(1);
    }

    @Test
    void staleCopyLosesWithOptimisticLocking() {
        Long id = orders.save(newOrder()).getId();
        PurchaseOrder alice = orders.findById(id).orElseThrow();   // both read version 0
        PurchaseOrder bob = orders.findById(id).orElseThrow();

        alice.setStatus(Status.PAID);
        orders.save(alice);                                        // version 0 -> 1
        System.out.println("alice saved, version now " + alice.getVersion());

        bob.setStatus(Status.SHIPPED);
        assertThatThrownBy(() -> orders.save(bob))                 // still has version 0
                .isInstanceOf(OptimisticLockingFailureException.class)
                .satisfies(e -> System.out.println("bob: " + e.getClass().getSimpleName() + ": " + e.getMessage()));
    }

    @Test
    void derivedAndCustomQueries() {
        orders.save(newOrder());
        PurchaseOrder other = new PurchaseOrder(2L, null);          // no address: ship_* columns stay NULL
        other.addLine("BOOK-1", 2, new BigDecimal("40.00"));
        other = orders.save(other);

        System.out.println("findByCustomerId(2): " + orders.findByCustomerId(2L).size() + " order(s)");
        System.out.println("shipTo of order without address: " + orders.findById(other.getId()).orElseThrow().getShipTo());

        boolean updated = orders.updateStatus(other.getId(), Status.PAID.name());
        System.out.println("updateStatus -> " + updated + ", findByStatus(PAID): " + orders.findByStatus(Status.PAID).size());

        orders.totals().forEach(t -> System.out.println("total: " + t));
        assertThat(orders.totals()).extracting(OrderTotal::total)
                .usingElementComparator(BigDecimal::compareTo)
                .containsExactly(new BigDecimal("50.00"), new BigDecimal("80.00"));
    }
}
