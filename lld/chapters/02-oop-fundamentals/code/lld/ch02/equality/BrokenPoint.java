package lld.ch02.equality;

/**
 * WRONG on purpose: overrides equals but NOT hashCode, breaking the contract "equal objects -> equal hash codes".
 * javac -Xlint warns about exactly this bug ([overrides]); the warning is silenced ONLY because this class
 * exists to demonstrate the bug. In real code, treat that warning as an error and add hashCode().
 */
@SuppressWarnings("overrides")
public final class BrokenPoint {
    private final int x, y;

    public BrokenPoint(int x, int y) { this.x = x; this.y = y; }

    @Override
    public boolean equals(Object o) {
        return o instanceof BrokenPoint p && x == p.x && y == p.y;
    }
    // hashCode() inherited from Object: usually different for two distinct objects
}
