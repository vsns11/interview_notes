package lld.ch04.srp.before;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 * VIOLATION: three different groups (actors) can each demand a change here.
 * Finance owns the pay rule, operations owns the report, the database team owns storage.
 */
public class Employee {
    private final String name;
    private final int hoursWorked;
    private final BigDecimal hourlyRate; // money: BigDecimal, never double

    public Employee(String name, int hoursWorked, BigDecimal hourlyRate) {
        this.name = name;
        this.hoursWorked = hoursWorked;
        this.hourlyRate = hourlyRate;
    }

    // Actor 1: Finance -- "overtime should start after 38 hours, not 40"
    public BigDecimal calculatePay() {
        int overtime = Math.max(0, hoursWorked - 40);
        int normal = hoursWorked - overtime;
        BigDecimal normalPay = hourlyRate.multiply(BigDecimal.valueOf(normal));
        BigDecimal overtimePay = hourlyRate.multiply(new BigDecimal("1.5"))
                .multiply(BigDecimal.valueOf(overtime));
        return normalPay.add(overtimePay).setScale(2, RoundingMode.HALF_EVEN);
    }

    // Actor 2: Operations -- "show the week-ending date in the report"
    public String reportHours() {
        return name + " worked " + hoursWorked + " hours";
    }

    // Actor 3: Database team -- "move from SQL to a document store"
    public void save() {
        System.out.println("INSERT INTO employees VALUES ('" + name + "')");
    }
}
