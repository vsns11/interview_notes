package lld.ch11.strategy;

/**
 * STRATEGY interface: one small question. Returns the delay in ms before the given
 * attempt (attempt >= 2), or STOP. One abstract method, so a lambda can be a policy.
 */
@FunctionalInterface
public interface RetryPolicy {
    int STOP = -1;

    int delayBeforeAttempt(int attempt);

    static RetryPolicy none() { return attempt -> STOP; }
}
