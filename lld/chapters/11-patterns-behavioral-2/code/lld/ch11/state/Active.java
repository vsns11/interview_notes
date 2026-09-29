package lld.ch11.state;

public record Active() implements SubscriptionState {
    public String name() { return "active"; }
    public void pay(Subscription s) { System.out.println("  [active] renewed for another period"); }
    public void missPayment(Subscription s) {
        System.out.println("  [active] payment failed, grace period started");
        s.setState(new PastDue());
    }
    public void cancel(Subscription s) { s.setState(new Cancelled()); }
}
