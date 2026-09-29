package lld.ch21.inventory;

/** Append-only ledger line. Stock levels can always be rebuilt by replaying these. */
public record StockMovement(int id, MovementType type, String sku, String fromWarehouse, String toWarehouse,
                            int quantity, String reference) {
    @Override public String toString() {
        return "#%d %-10s %s qty=%+d %s->%s (%s)".formatted(id, type, sku, quantity,
                fromWarehouse == null ? "-" : fromWarehouse, toWarehouse == null ? "-" : toWarehouse, reference);
    }
}
