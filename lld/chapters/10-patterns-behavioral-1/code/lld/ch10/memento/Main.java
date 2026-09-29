package lld.ch10.memento;

import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Checkpoint, bad rollout, rollback ===");
        DeploymentConfig config = new DeploymentConfig(3, "api:1.4", Map.of("LOG_LEVEL", "info"));
        DeploymentHistory history = new DeploymentHistory(2);
        System.out.println("  live: " + config);

        history.checkpoint(config, "before 2.0");
        config.apply(10, "api:2.0", Map.of("LOG_LEVEL", "debug"));   // the risky rollout
        config.setEnv("FEATURE_X", "on");                             // mutates the live map after saving
        System.out.println("  live: " + config);
        history.rollback(config);
        System.out.println("  live: " + config + "   <- snapshot was a real copy");

        System.out.println("\n=== 2. Bounded history ===");
        history.checkpoint(config, "v1");
        history.checkpoint(config, "v2");
        history.checkpoint(config, "v3");      // drops v1
        history.rollback(config);
        history.rollback(config);
        history.rollback(config);

        System.out.println("\n=== 3. The shallow-copy bug, shown in isolation ===");
        Map<String, String> liveEnv = new HashMap<>(Map.of("LOG_LEVEL", "info"));
        Map<String, String> badSnapshot = liveEnv;                  // shares the live map
        Map<String, String> goodSnapshot = new HashMap<>(liveEnv);  // owns a copy
        liveEnv.put("LOG_LEVEL", "debug");
        System.out.println("  bad snapshot  = " + badSnapshot + "  <- changed with the live object");
        System.out.println("  good snapshot = " + goodSnapshot);

        System.out.println("\n=== 4. The caretaker cannot open a snapshot ===");
        DeploymentConfig.Snapshot s = config.save("peek");
        System.out.println("  caretaker can read only: label=" + s.label());
        try {
            config.restore(() -> "forged");                        // a Snapshot not made by the originator
        } catch (IllegalArgumentException e) {
            System.out.println("  forged snapshot refused: " + e.getMessage());
        }
    }
}
