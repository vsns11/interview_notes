import java.util.*;

/**
 * Saga Pattern — runnable demo (orchestration style).
 *
 *   javac SagaDemo.java && java SagaDemo
 *
 * Shows:
 *   1. A successful saga across three services
 *   2. Failure at step 3 -> compensations run in REVERSE order
 *   3. A saga log, so a crash can be recovered
 *   4. Idempotent steps: a retried step does not apply twice
 *   5. Compensation is NOT rollback - the refund is a second real movement
 */
public class SagaDemo {

    // ---------- Saga log: what lets you recover after a crash ----------
    enum StepState { STARTED, COMPLETED, COMPENSATED }

    record LogEntry(String sagaId, String step, StepState state) {}

    static class SagaLog {
        private final List<LogEntry> entries = new ArrayList<>();

        void record(String sagaId, String step, StepState state) {
            entries.add(new LogEntry(sagaId, step, state));
            System.out.printf("      [log] %-18s %s%n", step, state);
        }

        /** Steps that completed and have not yet been compensated, newest first. */
        List<String> stepsNeedingCompensation(String sagaId) {
            List<String> completed = new ArrayList<>();
            for (LogEntry e : entries) {
                if (!e.sagaId().equals(sagaId)) continue;
                if (e.state() == StepState.COMPLETED)   completed.add(e.step());
                if (e.state() == StepState.COMPENSATED) completed.remove(e.step());
            }
            Collections.reverse(completed);          // undo in reverse order
            return completed;
        }
    }

    // ---------- Participant services, each with its own "database" ----------
    static class OrderService {
        private final Map<String, String> orders = new HashMap<>();

        void createOrder(String sagaId, String item) {
            orders.put(sagaId, "CONFIRMED:" + item);
            System.out.println("      OrderService: order created");
        }

        void cancelOrder(String sagaId) {                       // COMPENSATION
            orders.put(sagaId, "CANCELLED");
            System.out.println("      OrderService: order CANCELLED (compensation)");
        }

        String status(String sagaId) { return orders.getOrDefault(sagaId, "NONE"); }
    }

    static class PaymentService {
        private final Map<String, Integer> charges = new HashMap<>();
        private final List<String> ledger = new ArrayList<>();

        /** Idempotent: the sagaId acts as the idempotency key. */
        void charge(String sagaId, int amount) {
            if (charges.containsKey(sagaId)) {
                System.out.println("      PaymentService: duplicate detected, NOT charging again");
                return;
            }
            charges.put(sagaId, amount);
            ledger.add("CHARGE  +" + amount);
            System.out.println("      PaymentService: charged " + amount);
        }

        void refund(String sagaId) {                            // COMPENSATION
            Integer amount = charges.remove(sagaId);
            if (amount != null) {
                ledger.add("REFUND  -" + amount);
                System.out.println("      PaymentService: REFUNDED " + amount + " (compensation)");
            }
        }

        List<String> ledger() { return ledger; }
    }

    static class InventoryService {
        private int stock;
        private final Map<String, Integer> reserved = new HashMap<>();

        InventoryService(int stock) { this.stock = stock; }

        boolean reserve(String sagaId, int qty) {
            if (reserved.containsKey(sagaId)) {                 // idempotent
                System.out.println("      InventoryService: already reserved for this saga");
                return true;
            }
            if (stock < qty) {
                System.out.println("      InventoryService: INSUFFICIENT STOCK (have "
                        + stock + ", need " + qty + ")");
                return false;
            }
            stock -= qty;
            reserved.put(sagaId, qty);
            System.out.println("      InventoryService: reserved " + qty + " (stock now " + stock + ")");
            return true;
        }

        void release(String sagaId) {                           // COMPENSATION
            Integer qty = reserved.remove(sagaId);
            if (qty != null) {
                stock += qty;
                System.out.println("      InventoryService: RELEASED " + qty
                        + " (stock now " + stock + ") (compensation)");
            }
        }

        int stock() { return stock; }
    }

    // ---------- The orchestrator ----------
    static class OrderSagaOrchestrator {
        private final OrderService orders;
        private final PaymentService payments;
        private final InventoryService inventory;
        private final SagaLog log;

        OrderSagaOrchestrator(OrderService o, PaymentService p, InventoryService i, SagaLog l) {
            this.orders = o; this.payments = p; this.inventory = i; this.log = l;
        }

        boolean process(String sagaId, String item, int qty, int amount) {
            System.out.println("    --- saga " + sagaId + " starting ---");
            try {
                log.record(sagaId, "createOrder", StepState.STARTED);
                orders.createOrder(sagaId, item);
                log.record(sagaId, "createOrder", StepState.COMPLETED);

                log.record(sagaId, "chargePayment", StepState.STARTED);
                payments.charge(sagaId, amount);
                log.record(sagaId, "chargePayment", StepState.COMPLETED);

                log.record(sagaId, "reserveStock", StepState.STARTED);
                if (!inventory.reserve(sagaId, qty)) {
                    throw new RuntimeException("out of stock");
                }
                log.record(sagaId, "reserveStock", StepState.COMPLETED);

                System.out.println("    --- saga " + sagaId + " COMPLETED ---");
                return true;

            } catch (RuntimeException e) {
                System.out.println("    !!! step failed: " + e.getMessage() + " -> compensating");
                compensate(sagaId);
                System.out.println("    --- saga " + sagaId + " COMPENSATED ---");
                return false;
            }
        }

        /** Undo completed steps in reverse order, driven by the log. */
        void compensate(String sagaId) {
            for (String step : log.stepsNeedingCompensation(sagaId)) {
                switch (step) {
                    case "reserveStock"  -> inventory.release(sagaId);
                    case "chargePayment" -> payments.refund(sagaId);
                    case "createOrder"   -> orders.cancelOrder(sagaId);
                }
                log.record(sagaId, step, StepState.COMPENSATED);
            }
        }
    }

    public static void main(String[] args) {
        OrderService orders    = new OrderService();
        PaymentService payments = new PaymentService();
        InventoryService inv    = new InventoryService(5);
        SagaLog log             = new SagaLog();
        var saga = new OrderSagaOrchestrator(orders, payments, inv, log);

        System.out.println("\n=== 1. Happy path: all three steps succeed ===");
        saga.process("saga-1", "headphones", 2, 300);
        System.out.println("  order status: " + orders.status("saga-1")
                + " | stock: " + inv.stock());

        System.out.println("\n=== 2. Step 3 fails -> compensations run in REVERSE ===");
        saga.process("saga-2", "headphones", 99, 500);      // only 3 left
        System.out.println("  order status: " + orders.status("saga-2")
                + " | stock: " + inv.stock());
        System.out.println("  >> order cancelled AND payment refunded automatically");

        System.out.println("\n=== 3. Idempotency: the same step retried ===");
        payments.charge("saga-3", 150);
        payments.charge("saga-3", 150);       // network retry - must not double charge
        payments.charge("saga-3", 150);

        System.out.println("\n=== 4. Compensation is NOT rollback ===");
        System.out.println("  payment ledger: " + payments.ledger());
        System.out.println("  >> the charge AND the refund both exist. A rollback would have");
        System.out.println("     erased history; compensation adds an offsetting entry.");
        System.out.println("     The customer sees both lines on their statement.");
    }
}
