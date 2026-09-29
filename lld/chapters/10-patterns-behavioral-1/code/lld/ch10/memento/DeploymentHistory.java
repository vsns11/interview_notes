package lld.ch10.memento;

import java.util.ArrayDeque;
import java.util.Deque;

/** The CARETAKER: decides WHEN to save/restore, never WHAT is inside. Bounded to avoid leaks. */
public final class DeploymentHistory {
    private final Deque<DeploymentConfig.Snapshot> checkpoints = new ArrayDeque<>();
    private final int maxDepth;

    public DeploymentHistory(int maxDepth) { this.maxDepth = maxDepth; }

    public void checkpoint(DeploymentConfig config, String label) {
        if (checkpoints.size() == maxDepth) {
            DeploymentConfig.Snapshot dropped = checkpoints.removeLast();   // oldest
            System.out.println("  [history] full, dropped oldest checkpoint '" + dropped.label() + "'");
        }
        checkpoints.push(config.save(label));
        System.out.println("  [history] checkpoint '" + label + "' taken");
    }

    public void rollback(DeploymentConfig config) {
        if (checkpoints.isEmpty()) { System.out.println("  [history] nothing to roll back to"); return; }
        DeploymentConfig.Snapshot s = checkpoints.pop();
        config.restore(s);
        System.out.println("  [history] rolled back to '" + s.label() + "'");
    }
}
