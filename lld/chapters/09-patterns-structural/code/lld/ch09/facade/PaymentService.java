package lld.ch09.facade;

final class PaymentService {
    /** Declines anything over $100 so the demo can show the failure path. */
    boolean charge(String customer, long cents) {
        boolean ok = cents <= 10_000;
        System.out.println("  [payment]   charge " + customer + " " + cents + "c -> " + (ok ? "OK" : "DECLINED"));
        return ok;
    }
}
