package lld.ch21.inventory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        InventoryService inv = new InventoryService();
        inv.addListener((p, total, suggest) ->
                System.out.println("  [ALERT] " + p.sku() + " low: " + total + " left, reorder " + suggest + " to reach " + p.maxLevel()));

        inv.addWarehouse(new Warehouse("BLR-1", "Bengaluru"));
        inv.addWarehouse(new Warehouse("BLR-2", "Bengaluru"));
        inv.addWarehouse(new Warehouse("DEL-1", "Delhi"));
        inv.addProduct(new Product("PHONE", "Phone X", 20, 100));
        inv.addProduct(new Product("CASE", "Phone case", 10, 200));

        System.out.println("== receive stock");
        inv.receive("BLR-1", "PHONE", 30, "PO-1");
        inv.receive("DEL-1", "PHONE", 40, "PO-2");
        inv.receive("BLR-2", "PHONE", 5, "PO-3");
        System.out.println("  PHONE: " + inv.stockBySku("PHONE") + " total=" + inv.total("PHONE"));

        System.out.println("== ship 32 phones to a Bengaluru customer (nearest city first)");
        System.out.println("  " + inv.ship("PHONE", 32, new NearestCityFirst("Bengaluru"), "ORD-1"));
        System.out.println("  PHONE: " + inv.stockBySku("PHONE"));

        System.out.println("== transfer 15 phones DEL-1 -> BLR-1");
        inv.transfer("PHONE", "DEL-1", "BLR-1", 15, "TR-1");
        System.out.println("  PHONE: " + inv.stockBySku("PHONE"));

        System.out.println("== ship 25 more (most stock first) -> crosses reorder point");
        System.out.println("  " + inv.ship("PHONE", 25, new MostStockFirst(), "ORD-2"));
        System.out.println("  ship 1 more (no duplicate alert): " + inv.ship("PHONE", 1, new MostStockFirst(), "ORD-3"));

        System.out.println("== cycle count finds 2 damaged units in BLR-1");
        inv.adjust("BLR-1", "PHONE", inv.stockBySku("PHONE").get("BLR-1") - 2, "damaged");
        System.out.println("  PHONE: " + inv.stockBySku("PHONE") + " total=" + inv.total("PHONE"));

        System.out.println("== restock re-arms the alert");
        inv.receive("BLR-1", "PHONE", 90, "PO-4");
        inv.ship("PHONE", 90, new MostStockFirst(), "ORD-4");

        System.out.println("== edge cases");
        attempt(() -> inv.ship("PHONE", 999, new MostStockFirst(), "ORD-X"));
        attempt(() -> inv.transfer("PHONE", "BLR-2", "BLR-2", 1, "TR-X"));
        attempt(() -> inv.receive("MUM-1", "PHONE", 5, "PO-X"));
        attempt(() -> inv.receive("BLR-1", "TABLET", 5, "PO-X"));
        attempt(() -> inv.receive("BLR-1", "PHONE", 0, "PO-X"));

        System.out.println("== movement ledger for PHONE");
        inv.movements("PHONE").forEach(m -> System.out.println("  " + m));

        System.out.println("== concurrency: 200 one-unit orders race for 50 cases");
        inv.receive("BLR-1", "CASE", 50, "PO-5");
        AtomicInteger ok = new AtomicInteger(), rejected = new AtomicInteger();
        ExecutorService pool = Executors.newFixedThreadPool(8);
        for (int i = 0; i < 200; i++) {
            pool.submit(() -> {
                try { inv.ship("CASE", 1, new MostStockFirst(), "web"); ok.incrementAndGet(); }
                catch (InventoryException e) { rejected.incrementAndGet(); }
            });
        }
        pool.shutdown();
        pool.awaitTermination(10, TimeUnit.SECONDS);
        System.out.println("  shipped=" + ok + " rejected=" + rejected + " left=" + inv.total("CASE"));
    }

    private static void attempt(Runnable r) {
        try { r.run(); System.out.println("  ok"); }
        catch (InventoryException e) { System.out.println("  error: " + e.getMessage()); }
    }
}
