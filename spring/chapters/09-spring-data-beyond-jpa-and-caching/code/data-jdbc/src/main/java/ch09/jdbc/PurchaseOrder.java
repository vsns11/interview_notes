package ch09.jdbc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.jdbc.core.mapping.AggregateReference;
import org.springframework.data.relational.core.mapping.Embedded;
import org.springframework.data.relational.core.mapping.MappedCollection;

/** Aggregate root: the ONLY entry point to its order lines. */
public class PurchaseOrder {

    @Id Long id;
    @Version Integer version;                                   // optimistic locking; null = new
    AggregateReference<Customer, Long> customerId;             // another aggregate: id only, never loaded
    Status status = Status.NEW;
    @Embedded.Nullable(prefix = "ship_") Address shipTo;      // value object in the same row
    @CreatedDate Instant createdAt;                             // filled by auditing

    // Names written in annotations are used exactly as given AND quoted. Upper case matches H2's
    // unquoted DDL (and Oracle/DB2); on PostgreSQL/MySQL write them in lower case.
    @MappedCollection(idColumn = "PURCHASE_ORDER_ID", keyColumn = "LINE_NO")
    List<OrderLine> lines = new ArrayList<>();                 // owned children (table order_line)

    PurchaseOrder() {}                                          // for Spring Data

    public PurchaseOrder(long customerId, Address shipTo) {
        this.customerId = AggregateReference.to(customerId);
        this.shipTo = shipTo;
    }

    /** Business rules live in the root: all changes to lines go through it. */
    public void addLine(String sku, int quantity, BigDecimal unitPrice) {
        if (status != Status.NEW) throw new IllegalStateException("order is " + status);
        if (quantity <= 0) throw new IllegalArgumentException("quantity must be positive");
        lines.add(new OrderLine(sku, quantity, unitPrice));
    }

    public BigDecimal total() {
        return lines.stream().map(l -> l.unitPrice().multiply(BigDecimal.valueOf(l.quantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public Long getId() { return id; }
    public Integer getVersion() { return version; }
    public AggregateReference<Customer, Long> getCustomerId() { return customerId; }
    public Status getStatus() { return status; }
    public void setStatus(Status status) { this.status = status; }
    public Address getShipTo() { return shipTo; }
    public Instant getCreatedAt() { return createdAt; }
    public List<OrderLine> getLines() { return List.copyOf(lines); }
}
