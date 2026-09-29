package ch02.strings;

/** The essentials of String; full API in Chapter 7. */
public class StringBasics {
    public static void main(String[] args) {
        String greeting = "Hello";
        String name = "Ada";
        String message = greeting + ", " + name + "!";      // concatenation creates a new String
        System.out.println(message + " length=" + message.length());
        System.out.println("charAt(0)=" + message.charAt(0) + " indexOf(\"Ada\")=" + message.indexOf("Ada")
                + " substring(7, 10)=" + message.substring(7, 10));
        System.out.println("upper=" + message.toUpperCase() + " contains Ada? " + message.contains("Ada"));

        String s = "java";
        s.toUpperCase();                        // result thrown away: Strings are immutable
        System.out.println("after s.toUpperCase(): " + s);
        s = s.toUpperCase();                    // reassign the variable to the new String
        System.out.println("after s = s.toUpperCase(): " + s);

        String a = "hi", b = "hi", c = new String("hi");
        System.out.println("a == b: " + (a == b) + " (same pooled literal), a == c: " + (a == c)
                + ", a.equals(c): " + a.equals(c) + ", \"HI\".equalsIgnoreCase(a): " + "HI".equalsIgnoreCase(a));
        System.out.println("compareTo: " + "apple".compareTo("banana") + ", isBlank: " + "  ".isBlank()
                + ", strip: [" + "  pad  ".strip() + "], repeat: " + "ab".repeat(3));
        System.out.println("String.valueOf(42) + 1 = " + String.valueOf(42) + 1 + ", Integer.parseInt(\"42\") + 1 = "
                + (Integer.parseInt("42") + 1));
        System.out.println("'a' + 'b' = " + ('a' + 'b') + ", \"\" + 'a' + 'b' = " + ("" + 'a' + 'b'));
        String nothing = null;
        System.out.println("null concatenated: " + nothing);
    }
}
