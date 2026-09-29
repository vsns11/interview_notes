package lld.ch04.isp;

import java.util.List;
import lld.ch04.isp.roles.BasicPrinter;
import lld.ch04.isp.roles.OfficeDevice;
import lld.ch04.isp.roles.OfficeWorkflow;
import lld.ch04.isp.roles.PrintQueue;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Fat interface ===");
        List<lld.ch04.isp.fat.Printer> printers = List.of(
                new lld.ch04.isp.fat.AdvancedPrinter(), new lld.ch04.isp.fat.BasicPrinter());
        for (var p : printers) {
            p.printDocument("report.pdf");
            try {
                p.scanDocument("report.pdf");
            } catch (UnsupportedOperationException e) {
                System.out.println("  forced to implement: " + e.getMessage());
            }
        }

        System.out.println("=== 2. One interface per method ===");
        new lld.ch04.isp.split.BasicPrinter().printDocument("memo.pdf");
        var adv = new lld.ch04.isp.split.AdvancedPrinter();
        adv.faxDocument("memo.pdf");
        // The same object passed three times -- a sign the split went too far.
        new lld.ch04.isp.split.OfficeWorkflow(adv, adv, adv).run("contract.pdf");

        System.out.println("=== 3. Role interfaces (grouped by caller) ===");
        new PrintQueue(new BasicPrinter()).submit("receipt.pdf");
        new PrintQueue(new OfficeDevice()).submit("receipt.pdf"); // bigger device fits the small role
        new OfficeWorkflow(new OfficeDevice()).run("contract.pdf");
    }
}
