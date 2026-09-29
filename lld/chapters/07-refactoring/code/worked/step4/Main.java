package lld.ch07.worked.step4;

import java.util.List;

/** STEP 4 - Replace Primitive with Object (ServiceType enum) + Replace Magic Literal. */
public class Main {

    public static String run() {
        Invoice invoice = new Invoice("Asha Traders", List.of(
                new Shipment(ServiceType.fromCode("STD"), 4.0, 250),
                new Shipment(ServiceType.fromCode("EXP"), 2.5, 80),
                new Shipment(ServiceType.fromCode("FRG"), 12.0, 40),
                new Shipment(ServiceType.fromCode("STD"), 1.0, 90),
                new Shipment(ServiceType.fromCode("STD"), 0.5, 100),
                new Shipment(ServiceType.fromCode("FRG"), 10.0, 150)));
        return new TextRenderer().render(new StatementCalculator().calculate(invoice));
    }

    public static void main(String[] args) {
        System.out.print(run());
    }
}
