package ch03.patterns;

/** Pattern matching for instanceof (Java 16). */
public class InstanceofPattern {
    static String oldStyle(Object o) {
        if (o instanceof String) {
            String s = (String) o;                       // test, then cast: repeated type
            return "String of length " + s.length();
        }
        return "not a String";
    }

    static String newStyle(Object o) {
        if (o instanceof String s && !s.isEmpty()) {     // s is in scope on the right of &&
            return "String of length " + s.length();
        }
        if (o instanceof Integer i) return "Integer, doubled " + (i * 2);
        return "something else: " + o;
    }

    static int lengthOrZero(Object o) {
        if (!(o instanceof String s)) {
            return 0;                                    // leave early when it is NOT a String
        }
        return s.length();                               // flow scoping: s is definitely a String here
    }

    record Money(long cents, String currency) {
        @Override public boolean equals(Object other) {  // a classic use: equals()
            return other instanceof Money m && cents == m.cents && currency.equals(m.currency);
        }
    }

    public static void main(String[] args) {
        System.out.println(oldStyle("hello"));
        System.out.println(newStyle("hello") + " | " + newStyle(21) + " | " + newStyle("") + " | " + newStyle(null));
        System.out.println("lengthOrZero: " + lengthOrZero("abc") + ", " + lengthOrZero(3.14));
        System.out.println(new Money(500, "EUR").equals(new Money(500, "EUR")));
    }
}
