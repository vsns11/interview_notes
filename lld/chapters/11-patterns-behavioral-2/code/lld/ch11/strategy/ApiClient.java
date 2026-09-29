package lld.ch11.strategy;

import java.util.function.IntPredicate;

/** CONTEXT: holds a policy and never asks WHICH policy it has. */
public final class ApiClient {
    private final RetryPolicy policy;
    private final IntPredicate server;     // fake server: attempt number -> success?

    public ApiClient(RetryPolicy policy, IntPredicate server) {
        this.policy = policy;
        this.server = server;
    }

    public boolean call() {
        for (int attempt = 1; ; attempt++) {
            if (server.test(attempt)) {
                System.out.println("  attempt " + attempt + ": ok");
                return true;
            }
            int delay = policy.delayBeforeAttempt(attempt + 1);
            if (delay == RetryPolicy.STOP) {
                System.out.println("  attempt " + attempt + ": failed, giving up");
                return false;
            }
            System.out.println("  attempt " + attempt + ": failed, waiting " + delay + "ms");  // simulated, no sleep
        }
    }
}
