package lld.ch11.state;

public record PastDue() implements SubscriptionState {
    public String name() { return "past_due"; }
    public void pay(Subscription s) {
        System.out.println("  [past_due] arrears paid");
        s.setState(new Active());
    }
    public void missPayment(Subscription s) {
        System.out.println("  [past_due] grace period over");
        s.setState(new Cancelled());
    }
    public void cancel(Subscription s) { s.setState(new Cancelled()); }
}
