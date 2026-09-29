package lld.ch11.state;

/**
 * STATE interface. Each state implements all actions, including ones it only turns down.
 * Sealed: the compiler knows the full set of states.
 */
public sealed interface SubscriptionState permits Trialing, Active, PastDue, Cancelled {
    String name();
    void pay(Subscription s);
    void missPayment(Subscription s);
    void cancel(Subscription s);
}
