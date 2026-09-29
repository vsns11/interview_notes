package ch09.jdbc;

import java.util.List;
import org.springframework.data.jdbc.repository.query.Modifying;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.ListCrudRepository;

/** One repository per aggregate ROOT - there is no OrderLineRepository. */
public interface PurchaseOrderRepository extends ListCrudRepository<PurchaseOrder, Long> {

    List<PurchaseOrder> findByStatus(Status status);              // derived query

    List<PurchaseOrder> findByCustomerId(Long customerId);        // derived query on the reference column

    @Query("SELECT order_id, customer, total FROM order_totals ORDER BY order_id")
    List<OrderTotal> totals();                                    // hand-written SQL into a DTO

    @Modifying
    @Query("UPDATE purchase_order SET status = :status WHERE id = :id")
    boolean updateStatus(Long id, String status);                 // bulk-style update, bypasses the aggregate
}
