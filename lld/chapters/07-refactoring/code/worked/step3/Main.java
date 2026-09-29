package lld.ch07.worked.step3;

import java.util.List;

/** STEP 3 - Split Phase: calculate (StatementCalculator) then render (TextRenderer). */
public class Main {

    public static String run() {
        Invoice invoice = new Invoice("Asha Traders", List.of(
                new Shipment("STD", 4.0, 250),
                new Shipment("EXP", 2.5, 80),
                new Shipment("FRG", 12.0, 40),
                new Shipment("STD", 1.0, 90),
                new Shipment("STD", 0.5, 100),
                new Shipment("FRG", 10.0, 150)));
        return new TextRenderer().render(new StatementCalculator().calculate(invoice));
    }

    public static void main(String[] args) {
        System.out.print(run());
    }
}
