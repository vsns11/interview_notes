package lld.ch03.dependency;

/**
 * Dependency (dashed arrow, "uses"): CheckoutService has NO fields pointing at
 * Cart, PaymentGateway, TaxCalculator or Receipt. It only touches them for the
 * duration of one method call:
 *   - Cart and PaymentGateway arrive as PARAMETERS,
 *   - TaxCalculator is a LOCAL variable,
 *   - Receipt is the RETURN type.
 * When checkout() returns, the relationship is over.
 */
public final class CheckoutService {

    public Receipt checkout(Cart cart, PaymentGateway gateway) {
        TaxCalculator tax = new TaxCalculator();            // local-variable dependency
        Money subtotal = cart.subtotal();                   // parameter dependency
        Money total = subtotal.plus(tax.taxOn(subtotal));
        String txn = gateway.charge(total);                 // parameter dependency (interface)
        return new Receipt(txn, subtotal, total);           // return-type dependency
    }

    public record Receipt(String txnId, Money subtotal, Money total) { }
}
