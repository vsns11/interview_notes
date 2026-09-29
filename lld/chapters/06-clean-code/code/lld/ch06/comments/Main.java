/*
 * Copyright (c) 2026 Example Corp. Licensed under the Apache License, Version 2.0.   <-- GOOD: legal comment
 */
package lld.ch06.comments;

import java.util.List;
import java.util.regex.Pattern;

/** Comments: explain yourself in code first; keep only comments that code cannot express. */
public class Main {

    record Applicant(String name, int age, int creditScore, long monthlyIncomePaise, boolean hasDefaulted) {}

    // ---------------- BEFORE: comments used to excuse unclear code ----------------
    static class Before {
        // Changes:
        //   2019-03-01 RK: created
        //   2021-07-12 AB: fixed bug                                   <-- BAD: journal comment (git does this)
        /**
         * The eligible method.
         * @param a the a                                                <-- BAD: noise / mandated Javadoc
         * @return the boolean
         */
        static boolean eligible(Applicant a) {
            // check if applicant is eligible for a premium loan         <-- BAD: comment instead of a good name
            if (a.age() >= 21 && a.age() <= 60 && a.creditScore() > 750 && !a.hasDefaulted()
                    && a.monthlyIncomePaise() >= 5_000_000) {
                return true;   // return true                            <-- BAD: redundant
            }
            // if (a.age() > 65) return false;                           <-- BAD: commented-out code
            return false;
        } // end eligible                                                <-- BAD: closing-brace comment
    }

    // ---------------- AFTER: the code explains itself; comments add what code can't ----------------
    static class After {
        private static final int MIN_AGE = 21, MAX_AGE = 60;
        private static final int GOOD_CREDIT_SCORE = 750;
        private static final long MIN_MONTHLY_INCOME_PAISE = 5_000_000;   // Rs 50,000

        static boolean isEligibleForPremiumLoan(Applicant a) {
            return isWorkingAge(a) && hasGoodCredit(a) && earnsEnough(a);
        }
        private static boolean isWorkingAge(Applicant a) { return a.age() >= MIN_AGE && a.age() <= MAX_AGE; }
        private static boolean hasGoodCredit(Applicant a) { return a.creditScore() > GOOD_CREDIT_SCORE && !a.hasDefaulted(); }
        private static boolean earnsEnough(Applicant a) { return a.monthlyIncomePaise() >= MIN_MONTHLY_INCOME_PAISE; }

        // GOOD: informative comment -- clarifies what a regex matches (format: 10-character PAN, e.g. ABCDE1234F)
        private static final Pattern PAN_FORMAT = Pattern.compile("[A-Z]{5}[0-9]{4}[A-Z]");

        static boolean looksLikePan(String s) { return PAN_FORMAT.matcher(s).matches(); }

        /**
         * GOOD: Javadoc on a public API -- states the contract callers rely on.
         * @param scores credit scores in the range 300..900
         * @return the median score; for an even count, the lower of the two middle values
         * @throws IllegalArgumentException if {@code scores} is empty
         */
        static int medianScore(List<Integer> scores) {
            if (scores.isEmpty()) throw new IllegalArgumentException("no scores");
            // GOOD: explanation of intent -- we copy because callers may pass an unmodifiable list, and sorting in place would fail
            List<Integer> sorted = scores.stream().sorted().toList();
            return sorted.get((sorted.size() - 1) / 2);
        }

        // GOOD: warning of consequences -- this call hits the credit bureau and is billed per request.
        // TODO(loans-team): cache bureau responses for 24h once the bureau contract allows it.   <-- GOOD: TODO
        static int fetchBureauScore(String pan) { return 780; }
    }

    public static void main(String[] args) {
        var asha = new Applicant("Asha", 30, 790, 6_000_000, false);
        var ravi = new Applicant("Ravi", 30, 790, 6_000_000, true);
        System.out.println("BEFORE eligible(asha)=" + Before.eligible(asha) + ", eligible(ravi)=" + Before.eligible(ravi));
        System.out.println("AFTER  isEligibleForPremiumLoan(asha)=" + After.isEligibleForPremiumLoan(asha)
                + ", (ravi)=" + After.isEligibleForPremiumLoan(ravi));
        System.out.println("looksLikePan(ABCDE1234F)=" + After.looksLikePan("ABCDE1234F"));
        System.out.println("medianScore([700, 820, 650, 900])=" + After.medianScore(List.of(700, 820, 650, 900)));
        System.out.println("bureau score=" + After.fetchBureauScore("ABCDE1234F"));
    }
}
