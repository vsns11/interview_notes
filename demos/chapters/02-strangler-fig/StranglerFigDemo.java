import java.util.*;

/**
 * Strangler Fig Pattern — runnable demo.
 *
 *   javac StranglerFigDemo.java && java StranglerFigDemo
 *
 * Shows:
 *   1. A facade routing to legacy vs new per capability (feature flags)
 *   2. Percentage rollout via CONSISTENT hashing (same user -> same system)
 *   3. Instant rollback by flipping a flag
 */
public class StranglerFigDemo {

    // ---------- The two systems ----------
    interface BankingSystem {
        String createAccount(String name);
        String checkBalance(String accountId);
    }

    static class LegacySystem implements BankingSystem {
        public String createAccount(String name)      { return "LEGACY:created:" + name; }
        public String checkBalance(String accountId)  { return "LEGACY:balance:" + accountId; }
    }

    static class NewSystem implements BankingSystem {
        public String createAccount(String name)      { return "NEW:created:" + name; }
        public String checkBalance(String accountId)  { return "NEW:balance:" + accountId; }
    }

    // ---------- Feature flags: the migration control panel ----------
    static class FeatureFlags {
        private final Map<String, Integer> rolloutPercent = new HashMap<>();

        void setRollout(String capability, int percent) {
            rolloutPercent.put(capability, percent);
            System.out.printf("    [flags] %s -> %d%% on new system%n", capability, percent);
        }

        /** Consistent per user: hashing means user 42 always lands the same way. */
        boolean useNewSystem(String capability, String userId) {
            int percent = rolloutPercent.getOrDefault(capability, 0);
            if (percent <= 0)   return false;
            if (percent >= 100) return true;
            int bucket = Math.floorMod(userId.hashCode(), 100);
            return bucket < percent;
        }
    }

    // ---------- The facade: the only thing that changes at cutover ----------
    static class BankingFacade implements BankingSystem {
        private final LegacySystem legacy = new LegacySystem();
        private final NewSystem modern    = new NewSystem();
        private final FeatureFlags flags;
        private String currentUser = "anonymous";

        BankingFacade(FeatureFlags flags) { this.flags = flags; }

        void actingAs(String userId) { this.currentUser = userId; }

        public String createAccount(String name) {
            return flags.useNewSystem("createAccount", currentUser)
                    ? modern.createAccount(name)
                    : legacy.createAccount(name);
        }

        public String checkBalance(String accountId) {
            return flags.useNewSystem("checkBalance", currentUser)
                    ? modern.checkBalance(accountId)
                    : legacy.checkBalance(accountId);
        }
    }

    // ---------- Helper: how many of N users hit the new system? ----------
    static int countOnNewSystem(FeatureFlags flags, String capability, int users) {
        int n = 0;
        for (int i = 0; i < users; i++) {
            if (flags.useNewSystem(capability, "user-" + i)) n++;
        }
        return n;
    }

    public static void main(String[] args) {
        FeatureFlags flags = new FeatureFlags();
        BankingFacade facade = new BankingFacade(flags);

        System.out.println("\n=== Step 1: facade deployed, nothing migrated ===");
        System.out.println("  " + facade.createAccount("alice"));
        System.out.println("  " + facade.checkBalance("acc-1"));
        System.out.println("  >> all traffic still on LEGACY - facade proven harmless");

        System.out.println("\n=== Step 2: canary createAccount to 5% of users ===");
        flags.setRollout("createAccount", 5);
        System.out.printf("  users on new system: %d/1000%n",
                countOnNewSystem(flags, "createAccount", 1000));
        System.out.println("  >> checkBalance untouched: " + facade.checkBalance("acc-1"));

        System.out.println("\n=== Step 3: same user always gets the same system ===");
        facade.actingAs("user-7");
        for (int i = 0; i < 3; i++) {
            System.out.println("  user-7 -> " + facade.createAccount("acct"));
        }
        System.out.println("  >> consistent: differences never look like flaky bugs");

        System.out.println("\n=== Step 4: widen the rollout ===");
        for (int pct : new int[]{25, 50, 100}) {
            flags.setRollout("createAccount", pct);
            System.out.printf("      actual: %d/1000 users on new system%n",
                    countOnNewSystem(flags, "createAccount", 1000));
        }

        System.out.println("\n=== Step 5: problem found - INSTANT rollback ===");
        flags.setRollout("createAccount", 0);
        facade.actingAs("user-7");
        System.out.println("  user-7 -> " + facade.createAccount("acct"));
        System.out.println("  >> no deploy, no rebuild: config change only");

        System.out.println("\n=== Step 6: migrate the second capability ===");
        flags.setRollout("createAccount", 100);
        flags.setRollout("checkBalance", 100);
        facade.actingAs("user-7");
        System.out.println("  " + facade.createAccount("bob"));
        System.out.println("  " + facade.checkBalance("acc-1"));
        System.out.println("  >> legacy now receives zero traffic - safe to delete");
    }
}
