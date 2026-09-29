package lld.ch04.srp.after;

/** Only Operations asks for changes to this class. */
public class HoursReport {
    public String format(Employee e) {
        return e.name() + " worked " + e.hoursWorked() + " hours";
    }
}
