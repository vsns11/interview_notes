package lld.ch03.dependency;

public interface PaymentGateway {
    String charge(Money amount);   // returns a transaction id
}
