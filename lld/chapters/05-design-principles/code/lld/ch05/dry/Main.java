package lld.ch05.dry;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.regex.Pattern;

/** DRY: one piece of knowledge (what a valid email is, what the tax rate is) lives in ONE place. */
public class Main {

    // ---------------- BEFORE: the same rule is written twice and the copies have drifted ----------------
    static class Before {
        static class SignupService {
            boolean signup(String email) {
                // copy #1 of the rule (someone relaxed it later)
                return email != null && email.contains("@");
            }
            BigDecimal priceWithTax(BigDecimal price) {
                return price.multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP); // 18% hard-coded
            }
        }
        static class ProfileService {
            boolean changeEmail(String email) {
                // copy #2 of the rule (the stricter original)
                return email != null && email.matches("[^@\\s]+@[^@\\s]+\\.[a-z]{2,}");
            }
            BigDecimal priceWithTax(BigDecimal price) {
                return price.multiply(new BigDecimal("1.18")).setScale(2, RoundingMode.HALF_UP); // 18% again
            }
        }
    }

    // ---------------- AFTER: each rule has a single, authoritative home ----------------
    static class After {
        /** The ONLY definition of "valid email" in the system. */
        record EmailAddress(String value) {
            private static final Pattern FORMAT = Pattern.compile("[^@\\s]+@[^@\\s]+\\.[a-z]{2,}");
            EmailAddress {
                if (value == null || !FORMAT.matcher(value).matches())
                    throw new IllegalArgumentException("invalid email: " + value);
            }
        }

        /** The ONLY definition of the tax rule. */
        static final class TaxPolicy {
            private static final BigDecimal RATE = new BigDecimal("0.18");
            private TaxPolicy() {}
            static BigDecimal withTax(BigDecimal price) {
                return price.add(price.multiply(RATE)).setScale(2, RoundingMode.HALF_UP);
            }
        }

        static class SignupService {
            String signup(String rawEmail) { return "signed up " + new EmailAddress(rawEmail).value(); }
        }
        static class ProfileService {
            String changeEmail(String rawEmail) { return "email changed to " + new EmailAddress(rawEmail).value(); }
        }
    }

    public static void main(String[] args) {
        System.out.println("== BEFORE ==");
        var signup = new Before.SignupService();
        var profile = new Before.ProfileService();
        String sloppy = "bob@localhost";
        System.out.println("signup accepts '" + sloppy + "'? " + signup.signup(sloppy));
        System.out.println("profile accepts '" + sloppy + "'? " + profile.changeEmail(sloppy) + "   <-- same rule, two answers");

        System.out.println("== AFTER ==");
        var s = new After.SignupService();
        var p = new After.ProfileService();
        System.out.println(s.signup("bob@example.com"));
        System.out.println(p.changeEmail("bob@example.com"));
        for (String candidate : new String[]{sloppy}) {
            try { s.signup(candidate); } catch (IllegalArgumentException e) { System.out.println("signup rejected: " + e.getMessage()); }
            try { p.changeEmail(candidate); } catch (IllegalArgumentException e) { System.out.println("profile rejected: " + e.getMessage()); }
        }
        System.out.println("price 100.00 with tax = " + After.TaxPolicy.withTax(new BigDecimal("100.00")));
    }
}
