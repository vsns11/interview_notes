package lld.ch07.worked.step5;

import java.util.List;

/** STEP 5 (final) - Replace Conditional with Polymorphism + Move Function, then add the HTML feature. */
public class Main {

    static Invoice sampleInvoice() {
        return new Invoice("Asha Traders", List.of(
                new Shipment(ServiceType.fromCode("STD"), 4.0, 250),
                new Shipment(ServiceType.fromCode("EXP"), 2.5, 80),
                new Shipment(ServiceType.fromCode("FRG"), 12.0, 40),
                new Shipment(ServiceType.fromCode("STD"), 1.0, 90),
                new Shipment(ServiceType.fromCode("STD"), 0.5, 100),
                new Shipment(ServiceType.fromCode("FRG"), 10.0, 150)));
    }

    public static String run() {
        return new TextRenderer().render(new StatementCalculator().calculate(sampleInvoice()));
    }

    public static void main(String[] args) {
        System.out.print(run());
        System.out.println("--- new feature: HTML statement ---");
        System.out.print(new HtmlRenderer().render(new StatementCalculator().calculate(sampleInvoice())));
    }
}
