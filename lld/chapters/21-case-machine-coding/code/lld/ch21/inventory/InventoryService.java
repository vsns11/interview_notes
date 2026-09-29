package lld.ch21.inventory;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Stock is kept per (sku, warehouse). Locking is per SKU: every operation on one product
 * (even across warehouses) is serialized, while different products proceed in parallel.
 */
public class InventoryService {
    private final Map<String, Product> products = new ConcurrentHashMap<>();
    private final Map<String, Warehouse> warehouses = new ConcurrentHashMap<>();
    private final Map<String, Map<String, Integer>> stock = new ConcurrentHashMap<>();   // sku -> (wh -> qty)
    private final Map<String, ReentrantLock> skuLocks = new ConcurrentHashMap<>();
    private final Set<String> alerted = ConcurrentHashMap.newKeySet();                   // SKUs already flagged low
    private final Queue<StockMovement> ledger = new ConcurrentLinkedQueue<>();              // append-heavy, lock-free
    private final List<ReorderListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger movementSeq = new AtomicInteger();

    public void addListener(ReorderListener l) { listeners.add(l); }

    public void addProduct(Product p) {
        if (products.putIfAbsent(p.sku(), p) != null) throw new InventoryException("Duplicate SKU " + p.sku());
        stock.put(p.sku(), new LinkedHashMap<>());
        skuLocks.put(p.sku(), new ReentrantLock());
    }

    public void addWarehouse(Warehouse w) {
        if (warehouses.putIfAbsent(w.id(), w) != null) throw new InventoryException("Duplicate warehouse " + w.id());
    }

    public void receive(String warehouseId, String sku, int qty, String ref) {
        positive(qty);
        warehouse(warehouseId);
        withSku(sku, () -> {
            change(sku, warehouseId, qty);
            record(MovementType.INBOUND, sku, null, warehouseId, qty, ref);
        });
    }

    /** All-or-nothing: either the whole quantity is allocated across warehouses, or nothing moves. */
    public List<Allocation> ship(String sku, int qty, AllocationStrategy strategy, String orderRef) {
        positive(qty);
        return withSku(sku, () -> {
            Map<String, Integer> levels = stock.get(sku);
            int total = levels.values().stream().mapToInt(Integer::intValue).sum();
            if (total < qty) throw new InventoryException("Only " + total + " of " + sku + " available, order needs " + qty);
            List<Allocation> plan = new ArrayList<>();
            int remaining = qty;
            for (String wh : strategy.order(Map.copyOf(levels), warehouses)) {
                int take = Math.min(remaining, levels.get(wh));
                if (take > 0) { plan.add(new Allocation(wh, take)); remaining -= take; }
                if (remaining == 0) break;
            }
            for (Allocation a : plan) {
                change(sku, a.warehouseId(), -a.quantity());
                record(MovementType.OUTBOUND, sku, a.warehouseId(), null, -a.quantity(), orderRef);
            }
            return plan;
        });
    }

    public void transfer(String sku, String fromWh, String toWh, int qty, String ref) {
        positive(qty);
        warehouse(fromWh); warehouse(toWh);
        if (fromWh.equals(toWh)) throw new InventoryException("Source and target warehouse are the same");
        withSku(sku, () -> {
            int have = level(sku, fromWh);
            if (have < qty) throw new InventoryException(fromWh + " has only " + have + " of " + sku);
            change(sku, fromWh, -qty);
            change(sku, toWh, qty);
            record(MovementType.TRANSFER, sku, fromWh, toWh, qty, ref);
        });
    }

    /** Cycle count: set the level to what was physically counted and log the difference. */
    public void adjust(String warehouseId, String sku, int countedQty, String reason) {
        if (countedQty < 0) throw new InventoryException("Counted quantity cannot be negative");
        warehouse(warehouseId);
        withSku(sku, () -> {
            int delta = countedQty - level(sku, warehouseId);
            if (delta == 0) return;
            change(sku, warehouseId, delta);
            record(MovementType.ADJUSTMENT, sku, null, warehouseId, delta, reason);
        });
    }

    /** Sorted by warehouse id so output is stable. */
    public Map<String, Integer> stockBySku(String sku) {
        return withSku(sku, () -> java.util.Collections.unmodifiableMap(new java.util.TreeMap<>(stock.get(sku))));
    }

    public int total(String sku) { return withSku(sku, () -> totalUnlocked(sku)); }

    public List<StockMovement> movements(String sku) {
        return ledger.stream().filter(m -> m.sku().equals(sku)).toList();
    }

    // ------------------------------------------------------------ internals (called with the SKU lock held)

    private void change(String sku, String wh, int delta) {
        stock.get(sku).merge(wh, delta, Integer::sum);
    }

    private void record(MovementType type, String sku, String from, String to, int qty, String ref) {
        ledger.add(new StockMovement(movementSeq.incrementAndGet(), type, sku, from, to, qty, ref));
        checkReorder(sku);
    }

    /** Fires once when stock crosses down to the reorder point; re-arms when stock recovers. */
    private void checkReorder(String sku) {
        Product p = products.get(sku);
        int total = totalUnlocked(sku);
        if (total <= p.reorderPoint()) {
            if (alerted.add(sku)) listeners.forEach(l -> l.lowStock(p, total, p.maxLevel() - total));
        } else {
            alerted.remove(sku);
        }
    }

    private int totalUnlocked(String sku) { return stock.get(sku).values().stream().mapToInt(Integer::intValue).sum(); }
    private int level(String sku, String wh) { return stock.get(sku).getOrDefault(wh, 0); }

    private <T> T withSku(String sku, java.util.function.Supplier<T> action) {
        ReentrantLock lock = skuLocks.get(sku);
        if (lock == null) throw new InventoryException("Unknown SKU " + sku);
        lock.lock();
        try { return action.get(); } finally { lock.unlock(); }
    }

    private void withSku(String sku, Runnable action) { withSku(sku, () -> { action.run(); return null; }); }

    private void warehouse(String id) { if (!warehouses.containsKey(id)) throw new InventoryException("Unknown warehouse " + id); }
    private static void positive(int q) { if (q <= 0) throw new InventoryException("Quantity must be positive: " + q); }
}
