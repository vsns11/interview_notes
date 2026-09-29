package ch02.operators;

/** Relational, equality, logical, short-circuit, ternary, instanceof. */
public class Comparison {
    static boolean check(String label, boolean value) {
        System.out.println("  evaluated " + label);
        return value;
    }

    public static void main(String[] args) {
        int age = 20;
        System.out.println("age >= 18: " + (age >= 18) + ", age != 20: " + (age != 20));

        String s1 = "java";
        String s2 = new String("java");
        System.out.println("s1 == s2: " + (s1 == s2) + ", s1.equals(s2): " + s1.equals(s2));

        System.out.println("&& short-circuits:");
        boolean r1 = check("left", false) && check("right", true);
        System.out.println("& evaluates both:");
        boolean r2 = check("left", false) & check("right", true);
        System.out.println("r1=" + r1 + " r2=" + r2 + " true ^ true=" + (true ^ true) + " !true=" + !true);

        String name = null;
        if (name != null && name.length() > 3) System.out.println("long name");
        else System.out.println("null-safe check with && worked");

        String category = age < 13 ? "child" : age < 20 ? "teen" : "adult";
        System.out.println("category=" + category);

        Object o = "hello";
        System.out.println("o instanceof String: " + (o instanceof String)
                + ", null instanceof String: " + (null instanceof String));
    }
}
