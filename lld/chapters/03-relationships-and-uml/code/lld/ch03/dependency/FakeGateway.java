package lld.ch03.dependency;

public final class FakeGateway implements PaymentGateway {
    private int next = 1;
    @Override public String charge(Money amount) {
        System.out.println("  [gateway] charging " + amount);
        return "TXN-" + next++;
    }
}
