package lld.ch06.emergence;

import lld.ch06.testing.MiniTest;
import lld.ch06.testing.Test;

/**
 * Kent Beck's four rules of simple design, in the priority order Clean Code gives:
 *  1. runs all the tests   2. no duplication   3. expresses intent   4. fewest classes and methods
 */
public class Main {

    record Employee(String name, int monthsWorked, boolean onProbation) {}

    // ---------------- BEFORE: duplicated algorithm, magic numbers, needless layers ----------------
    static class Before {
        interface LeaveCalcStrategyProvider { LeaveCalc get(); }             // extra element with no purpose
        static class LeaveCalc {
            int calcIN(Employee e) {                                          // unclear name
                if (e.onProbation()) return 0;
                int d = e.monthsWorked() * 18 / 12;                           // 18 per year? magic
                if (d > 45) d = 45;                                           // 45 cap? magic
                return d;
            }
            int calcUS(Employee e) {                                          // the same algorithm, copied
                if (e.onProbation()) return 0;
                int d = e.monthsWorked() * 15 / 12;
                if (d > 30) d = 30;
                return d;
            }
        }
    }

    // ---------------- AFTER: one algorithm (Template Method), named rules, only needed elements ----------------
    static class After {
        abstract static class LeavePolicy {
            /** The shared algorithm, written once (rule 2: no duplication). */
            final int earnedLeaveDays(Employee e) {
                if (e.onProbation()) return 0;
                return Math.min(daysPerYear() * e.monthsWorked() / 12, maxCarryForwardDays());
            }
            protected abstract int daysPerYear();              // what varies, with intention-revealing names (rule 3)
            protected abstract int maxCarryForwardDays();
        }
        static final class IndiaLeavePolicy extends LeavePolicy {
            protected int daysPerYear() { return 18; }
            protected int maxCarryForwardDays() { return 45; }
        }
        static final class UsLeavePolicy extends LeavePolicy {
            protected int daysPerYear() { return 15; }
            protected int maxCarryForwardDays() { return 30; }
        }
    }

    /** Rule 1 first: the refactoring is only safe because these tests pass before AND after. */
    public static class LeavePolicyTest {
        private final Employee veteran = new Employee("Asha", 48, false);
        private final Employee newHire = new Employee("Ravi", 8, false);
        private final Employee probationer = new Employee("Kabir", 3, true);

        @Test void india_policy_matches_old_behaviour() {
            var old = new Before.LeaveCalc();
            var policy = new After.IndiaLeavePolicy();
            for (Employee e : new Employee[]{veteran, newHire, probationer})
                MiniTest.assertEquals(old.calcIN(e), policy.earnedLeaveDays(e));
        }
        @Test void us_policy_matches_old_behaviour() {
            var old = new Before.LeaveCalc();
            var policy = new After.UsLeavePolicy();
            for (Employee e : new Employee[]{veteran, newHire, probationer})
                MiniTest.assertEquals(old.calcUS(e), policy.earnedLeaveDays(e));
        }
        @Test void carry_forward_is_capped() {
            MiniTest.assertEquals(45, new After.IndiaLeavePolicy().earnedLeaveDays(veteran));
        }
        @Test void probationers_earn_nothing() {
            MiniTest.assertEquals(0, new After.UsLeavePolicy().earnedLeaveDays(probationer));
        }
    }

    public static void main(String[] args) {
        var summary = MiniTest.run(LeavePolicyTest.class);
        System.out.println(summary.passed() + " passed, " + summary.failed() + " failed");
        var in = new After.IndiaLeavePolicy();
        var us = new After.UsLeavePolicy();
        for (Employee e : new Employee[]{new Employee("Asha", 48, false), new Employee("Ravi", 8, false)})
            System.out.println(e.name() + ": India " + in.earnedLeaveDays(e) + " days, US " + us.earnedLeaveDays(e) + " days");
    }
}
