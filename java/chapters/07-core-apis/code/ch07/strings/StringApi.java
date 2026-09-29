package ch07.strings;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class StringApi {
    public static void main(String[] args) {
        String s = "  Hello, Java World  ";

        // 1. Inspecting
        System.out.println("length=" + s.length() + " isEmpty=" + s.isEmpty()
                + " isBlank(\"  \")=" + "  ".isBlank());
        String t = s.strip();                                  // Java 11, Unicode-aware trim
        System.out.println("strip -> [" + t + "] charAt(7)=" + t.charAt(7));

        // 2. Searching
        System.out.println("indexOf('o')=" + t.indexOf('o') + " lastIndexOf('o')=" + t.lastIndexOf('o')
                + " indexOf(\"Java\")=" + t.indexOf("Java") + " indexOf(\"Kotlin\")=" + t.indexOf("Kotlin"));
        System.out.println("contains=" + t.contains("Java") + " startsWith=" + t.startsWith("Hell")
                + " endsWith=" + t.endsWith("World"));

        // 3. Extracting and transforming (each returns a NEW string)
        System.out.println("substring(7)=" + t.substring(7) + " substring(7, 11)=" + t.substring(7, 11));
        System.out.println("replace=" + t.replace('o', '0') + " | replaceAll=" + t.replaceAll("[aeiou]", "*"));
        System.out.println("upper=" + t.toUpperCase() + " lower=" + t.toLowerCase());
        System.out.println("repeat=" + "ab".repeat(3) + " join=" + String.join("-", "a", "b", "c")
                + " joinList=" + String.join("/", List.of("usr", "local", "bin")));

        // 4. Comparing
        System.out.println("equals=" + "java".equals("Java") + " equalsIgnoreCase=" + "java".equalsIgnoreCase("Java")
                + " compareTo(\"apple\",\"banana\")=" + "apple".compareTo("banana")
                + " compareTo(\"b\",\"a\")=" + "b".compareTo("a")
                + " compareTo(\"Z\",\"a\")=" + "Z".compareTo("a")
                + " compareToIgnoreCase=" + "Z".compareToIgnoreCase("a"));

        // 5. Splitting
        System.out.println("split(\",\")=" + Arrays.toString("a,b,,c,,".split(",")));
        System.out.println("split(\",\", -1)=" + Arrays.toString("a,b,,c,,".split(",", -1)));
        System.out.println("split(\"\\\\s+\")=" + Arrays.toString("one  two\tthree".split("\\s+")));
        System.out.println("split(\".\") on 1.2.3 -> " + "1.2.3".split(".").length + " parts; split(\"\\\\.\") -> "
                + Arrays.toString("1.2.3".split("\\.")));
        System.out.println("splitWithDelimiters=" + Arrays.toString("a+b-c".splitWithDelimiters("[+-]", 0))); // Java 21

        // 6. Characters, chars() and lines()
        String word = "level";
        String reversed = new StringBuilder(word).reverse().toString();
        System.out.println("palindrome(" + word + ")=" + word.equals(reversed)
                + " toCharArray=" + Arrays.toString("abc".toCharArray())
                + " vowels=" + "education".chars().filter(c -> "aeiou".indexOf(c) >= 0).count());
        System.out.println("lines=" + "a\nb\r\nc".lines().collect(Collectors.toList())
                + " indent=[" + "x".indent(2).replace("\n", "\\n") + "]");
        System.out.println("formatted=" + "%s is %d".formatted("Ada", 36)
                + " valueOf(3.5)=" + String.valueOf(3.5) + " valueOf(char[])=" + String.valueOf(new char[]{'h', 'i'}));

        // 7. Unicode: length counts UTF-16 code units, not "characters"
        String emoji = "I ❤ Jäva 😀";   // heart, a-umlaut, grinning face (outside the BMP)
        System.out.println(emoji + " length=" + emoji.length() + " codePoints=" + emoji.codePointCount(0, emoji.length())
                + " charAt(9) is high surrogate? " + Character.isHighSurrogate(emoji.charAt(9)));
    }
}
