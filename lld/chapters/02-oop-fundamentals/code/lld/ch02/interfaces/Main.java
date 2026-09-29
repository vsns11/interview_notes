package lld.ch02.interfaces;

import java.util.List;
import java.util.function.Predicate;

public class Main {
    public static void main(String[] args) {
        Notifier email = new EmailNotifier();                 // class implementing the interface
        Notifier sms = (to, msg) -> "[sms] " + to + ": " + msg; // Notifier is functional too (one abstract method)
        for (Notifier n : List.of(email, sms, Notifier.console())) {
            System.out.println(n.send("ada", "your order shipped"));
        }
        System.out.println(email.sendAll(List.of("kai", "ravi"), "  sale starts today  "));

        DiscountRule tenPercent = p -> p * 90 / 100;
        DiscountRule minusFiveDollars = p -> Math.max(0, p - 500);
        System.out.println("10% then -$5 on 10000c = " + tenPercent.then(minusFiveDollars).apply(10_000) + "c");

        Predicate<String> blank = String::isBlank;              // JDK functional interface + method reference
        System.out.println("'  ' blank? " + blank.test("  "));

        Report r = new SalesReport(123_45);
        System.out.println(r.render());
    }
}
