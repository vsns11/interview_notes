package lld.ch04.srp;

import java.math.BigDecimal;
import lld.ch04.srp.after.EmployeeRepository;
import lld.ch04.srp.after.HoursReport;
import lld.ch04.srp.after.PayCalculator;

public class Main {
    public static void main(String[] args) {
        System.out.println("--- Before: one class, three actors ---");
        var before = new lld.ch04.srp.before.Employee("Ada", 45, new BigDecimal("20.00"));
        System.out.println("Pay: " + before.calculatePay());
        System.out.println(before.reportHours());
        before.save();

        System.out.println("--- After: split by actor ---");
        var ada = new lld.ch04.srp.after.Employee("Ada", 45, new BigDecimal("20.00"));
        System.out.println("Pay: " + new PayCalculator().calculatePay(ada));
        System.out.println(new HoursReport().format(ada));
        new EmployeeRepository().save(ada);
    }
}
