package lld.ch03.dependency;

import java.util.List;

public class Main {
    public static void main(String[] args) {
        CheckoutService service = new CheckoutService();   // holds no collaborators at all
        Cart cart = new Cart(List.of(Money.of("250"), Money.of("99.50")));

        // The caller decides which gateway to use on every call.
        CheckoutService.Receipt r = service.checkout(cart, new FakeGateway());
        System.out.println("Receipt: " + r);

        PaymentGateway loggingGateway = amount -> {
            System.out.println("  [lambda gateway] charging " + amount);
            return "LMB-42";
        };
        System.out.println("Receipt: " + service.checkout(cart, loggingGateway));
    }
}
