package lld.ch07.worked.step0;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/** STEP 0 - the messy starting point. It works, but it smells. */
public class Main {

    // Mutable public data class (smells: Data Class, Mysterious Name)
    static class Shp {
        public String t;   // "STD", "EXP" or "FRG"
        public double kg;
        public int km;
        Shp(String t, double kg, int km) { this.t = t; this.kg = kg; this.km = km; }
    }

    static class Inv {
        public String cust;
        public List<Shp> list = new ArrayList<>();
    }

    // One Long Function that calculates AND formats (smells: Long Function,
    // Repeated Switches waiting to happen, magic numbers, temps everywhere)
    static String print(Inv inv) {
        double tot = 0;
        int pts = 0;
        int unused = 0;                                   // dead code
        StringBuilder r = new StringBuilder("Invoice for " + inv.cust + "\n");
        for (Shp s : inv.list) {
            double amt = 0;
            switch (s.t) {
                case "STD":
                    amt = 5 + s.kg * 1.2;
                    if (s.km > 100) amt += (s.km - 100) * 0.05;
                    break;
                case "EXP":
                    amt = 12 + s.kg * 2.0;
                    if (s.km > 50) amt += (s.km - 50) * 0.10;
                    break;
                case "FRG":
                    amt = 8 + s.kg * 1.5 + 3;
                    if (s.kg > 10) amt = amt * 1.2;
                    break;
                default:
                    throw new IllegalArgumentException("unknown type: " + s.t);
            }
            // loyalty points
            pts += s.km / 100;
            if ("EXP".equals(s.t)) pts += 2;
            r.append("  ").append(s.t).append(" ").append(s.kg).append("kg ")
             .append(s.km).append("km: ")
             .append(String.format(Locale.ROOT, "%.2f", amt)).append("\n");
            tot += amt;
        }
        r.append("Amount owed: ").append(String.format(Locale.ROOT, "%.2f", tot)).append("\n");
        r.append("Points earned: ").append(pts).append("\n");
        return r.toString();
    }

    /** Used by the characterisation check in lld.ch07.worked.Main. */
    public static String run() {
        Inv inv = new Inv();
        inv.cust = "Asha Traders";
        inv.list.add(new Shp("STD", 4.0, 250));
        inv.list.add(new Shp("EXP", 2.5, 80));
        inv.list.add(new Shp("FRG", 12.0, 40));
        inv.list.add(new Shp("STD", 1.0, 90));
        inv.list.add(new Shp("STD", 0.5, 100));   // boundary: exactly 100 km
        inv.list.add(new Shp("FRG", 10.0, 150));  // boundary: exactly 10 kg
        return print(inv);
    }

    public static void main(String[] args) {
        System.out.print(run());
    }
}
