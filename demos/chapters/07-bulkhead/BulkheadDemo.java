import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class BulkheadDemo {

    static final int POOL = 10;
    static final int REQUESTS = 20;

    static String slowInventory() throws InterruptedException {
        Thread.sleep(2000);
        return "inventory-ok";
    }

    static String fastShipping() throws InterruptedException {
        Thread.sleep(50);
        return "shipping-ok";
    }

    static void withoutBulkheads() throws Exception {
        ExecutorService shared = Executors.newFixedThreadPool(POOL);
        AtomicInteger shippingDone = new AtomicInteger();
        long start = System.currentTimeMillis();

        for (int i = 0; i < REQUESTS; i++) {
            shared.submit(() -> { slowInventory(); return null; });
        }
        for (int i = 0; i < 5; i++) {
            shared.submit(() -> {
                fastShipping();
                shippingDone.incrementAndGet();
                return null;
            });
        }

        shared.shutdown();
        boolean finished = shared.awaitTermination(3, TimeUnit.SECONDS);
        System.out.printf("  shipping completed in 3s: %d/5   (finished=%s, %dms)%n",
                shippingDone.get(), finished, System.currentTimeMillis() - start);
        shared.shutdownNow();
    }

    static void withBulkheads() throws Exception {
        ExecutorService inventoryPool = Executors.newFixedThreadPool(POOL);
        ExecutorService shippingPool  = Executors.newFixedThreadPool(5);
        AtomicInteger shippingDone = new AtomicInteger();
        long start = System.currentTimeMillis();

        for (int i = 0; i < REQUESTS; i++) {
            inventoryPool.submit(() -> { slowInventory(); return null; });
        }
        for (int i = 0; i < 5; i++) {
            shippingPool.submit(() -> {
                fastShipping();
                shippingDone.incrementAndGet();
                return null;
            });
        }

        Thread.sleep(3000);
        System.out.printf("  shipping completed in 3s: %d/5   (%dms)%n",
                shippingDone.get(), System.currentTimeMillis() - start);
        inventoryPool.shutdownNow();
        shippingPool.shutdownNow();
    }

    public static void main(String[] args) throws Exception {
        System.out.println("\n=== WITHOUT bulkheads (shared pool of " + POOL + ") ===");
        withoutBulkheads();

        System.out.println("\n=== WITH bulkheads (separate pools) ===");
        withBulkheads();
    }
}
