package lld.ch04.dip.locator;

/** The high-level code depends on this abstraction, not on a concrete registry. */
public interface Locator {
    PaymentService paymentService(String name);
}
