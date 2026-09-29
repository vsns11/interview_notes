package lld.ch06.smells;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

/**
 * A handful of Clean Code heuristics applied to one function:
 * G25 magic numbers, G19 explanatory variables, G16 obscured intent, G28 encapsulate conditionals,
 * G29 avoid negative conditionals, F3 flag arguments, G26 be precise (no floating point for money),
 * N1/G20 descriptive names that say what the function does.
 */
public class Main {

    enum Membership { NONE, PRIME }
    record Parcel(double weightKg, int distanceKm, Membership membership, boolean fragile) {}

    // ---------------- BEFORE ----------------
    static class Before {
        static double calc(Parcel p, boolean express) {                                   // double for money (G26)
            double c;
            if (!(p.membership() != Membership.PRIME)) c = 0; else c = 40;           // double negative
            if (p.weightKg() > 20 || p.distanceKm() > 1500 && !p.fragile() == false) c += 250;   // what does this mean?
            c += p.weightKg() * 12.5;
            if (express) c = c * 1.5;                                                     // flag argument
            if (express && p.membership() == Membership.PRIME) c = c * 0.9;
            return Math.round(c * 100) / 100.0;
        }
    }

    // ---------------- AFTER ----------------
    static class After {
        enum Speed {                                               // replaces the flag; each speed knows its multiplier
            STANDARD("1.0"), EXPRESS("1.5");
            final BigDecimal multiplier;
            Speed(String m) { multiplier = new BigDecimal(m); }
        }

        private static final BigDecimal BASE_FEE = new BigDecimal("40");          // rupees, exact decimal (G26)
        private static final BigDecimal RATE_PER_KG = new BigDecimal("12.5");
        private static final double HEAVY_KG = 20;
        private static final int LONG_HAUL_KM = 1500;
        private static final BigDecimal SPECIAL_HANDLING_FEE = new BigDecimal("250");
        private static final BigDecimal PRIME_EXPRESS_DISCOUNT = new BigDecimal("0.9");

        static BigDecimal shippingCost(Parcel p, Speed speed) {
            BigDecimal baseFee = isPrime(p) ? BigDecimal.ZERO : BASE_FEE;
            BigDecimal handlingFee = needsSpecialHandling(p) ? SPECIAL_HANDLING_FEE : BigDecimal.ZERO;
            BigDecimal weightCharge = BigDecimal.valueOf(p.weightKg()).multiply(RATE_PER_KG);   // explanatory variables
            BigDecimal cost = baseFee.add(handlingFee).add(weightCharge).multiply(speed.multiplier);
            if (isPrime(p) && speed == Speed.EXPRESS) cost = cost.multiply(PRIME_EXPRESS_DISCOUNT);
            return cost.setScale(2, RoundingMode.HALF_UP);                           // round once, explicitly, to paise
        }

        private static boolean isPrime(Parcel p) { return p.membership() == Membership.PRIME; }   // positive conditional
        private static boolean needsSpecialHandling(Parcel p) {                                 // encapsulated conditional
            boolean heavy = p.weightKg() > HEAVY_KG;
            boolean fragileLongHaul = p.distanceKm() > LONG_HAUL_KM && p.fragile();
            return heavy || fragileLongHaul;
        }
    }

    public static void main(String[] args) {
        List<Parcel> parcels = List.of(
                new Parcel(2, 300, Membership.NONE, false),
                new Parcel(25, 100, Membership.PRIME, false),
                new Parcel(3, 2000, Membership.NONE, true),
                new Parcel(3, 2000, Membership.PRIME, false));
        for (Parcel p : parcels) {
            for (boolean express : new boolean[]{false, true}) {
                double before = Before.calc(p, express);
                BigDecimal after = After.shippingCost(p, express ? After.Speed.EXPRESS : After.Speed.STANDARD);
                boolean same = BigDecimal.valueOf(before).compareTo(after) == 0;
                System.out.printf("%-55s express=%-5s before=%7.2f after=%7s %s%n",
                        p, express, before, after, same ? "same" : "DIFFERENT");
            }
        }
    }
}
