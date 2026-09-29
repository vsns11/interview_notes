package ch07.strings;

public class StringPool {
    public static void main(String[] args) {
        String a = "hello";                      // literal -> string pool
        String b = "hello";                      // same pooled object
        String c = new String("hello");          // always a new heap object
        String d = c.intern();                   // pooled copy
        String e = "hel" + "lo";                 // constant expression, folded at compile time
        String part = "hel";
        String f = part + "lo";                  // computed at run time -> new object
        final String constPart = "hel";
        String g = constPart + "lo";             // final + literal initialiser = constant -> pooled

        System.out.println("a == b: " + (a == b));
        System.out.println("a == c: " + (a == c) + ", a.equals(c): " + a.equals(c));
        System.out.println("a == c.intern(): " + (a == d));
        System.out.println("a == \"hel\" + \"lo\": " + (a == e));
        System.out.println("a == part + \"lo\": " + (a == f));
        System.out.println("a == constPart + \"lo\": " + (a == g));

        // Immutability: "changing" methods return new objects
        String s = "java";
        String upper = s.toUpperCase();
        System.out.println("s=" + s + ", upper=" + upper + ", same object? " + (s == upper));
        System.out.println("\"x\".equals(null)=" + "x".equals(null) + ", Objects.equals(null,null)="
                + java.util.Objects.equals(null, null));
    }
}
