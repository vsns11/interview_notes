package lld.ch11.state;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Subscription lifecycle (class per state) ===");
        Subscription sub = new Subscription();
        sub.pay();           // trialing -> active
        sub.missPayment();   // active -> past_due
        sub.pay();           // past_due -> active
        sub.cancel();        // active -> cancelled
        sub.pay();           // rejected, stays cancelled
        System.out.println("  final status: " + sub.status());

        System.out.println("\n=== 2. Traffic light (enum-based state machine) ===");
        TrafficLight light = TrafficLight.RED;
        for (int i = 0; i < 4; i++) {
            System.out.printf("  %-6s for %2ds, pedestrians may cross: %s%n",
                light, light.seconds(), light.pedestriansMayCross());
            light = light.next();
        }

        System.out.println("\n=== 3. Vending machine ===");
        VendingMachine vm = new VendingMachine();
        vm.load("A1", "cola", 150, 1);
        vm.load("B2", "chips", 100, 0);
        vm.select("A1");        // idle: ignored
        vm.insertCoin(100);     // idle -> has_money
        vm.select("A1");        // not enough
        vm.select("B2");        // empty slot
        vm.insertCoin(100);
        vm.select("A1");        // dispense, change 50, -> sold_out (nothing left)
        vm.insertCoin(25);      // sold_out: rejected
    }
}
