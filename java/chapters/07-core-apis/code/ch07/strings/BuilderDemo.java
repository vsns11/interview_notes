package ch07.strings;

import java.util.StringJoiner;

public class BuilderDemo {
    public static void main(String[] args) {
        StringBuilder sb = new StringBuilder("Java");
        sb.append(' ').append(25).append(" LTS");          // methods return this -> chaining
        System.out.println("append:  " + sb + " (length " + sb.length() + ")");
        sb.insert(0, ">> ");
        System.out.println("insert:  " + sb);
        sb.replace(3, 7, "JAVA");
        System.out.println("replace: " + sb);
        sb.delete(0, 3);
        System.out.println("delete:  " + sb);
        sb.deleteCharAt(sb.length() - 1).setCharAt(0, 'j');   // deleteCharAt returns sb; setCharAt is void
        System.out.println("deleteCharAt+setCharAt: " + sb);
        System.out.println("reverse: " + new StringBuilder("stressed").reverse());
        System.out.println("indexOf(\"25\")=" + sb.indexOf("25") + " charAt(1)=" + sb.charAt(1));
        System.out.println("repeat (21): " + new StringBuilder().repeat("=-", 5).repeat('!', 2));

        // StringBuilder does NOT override equals: compare contents via toString() or compareTo (11)
        StringBuilder x = new StringBuilder("abc"), y = new StringBuilder("abc");
        System.out.println("x.equals(y)=" + x.equals(y) + " x.toString().equals(y.toString())="
                + x.toString().equals(y.toString()) + " x.compareTo(y)=" + x.compareTo(y)
                + " \"abc\".contentEquals(x)=" + "abc".contentEquals(x));

        // Building a string in a loop
        StringBuilder csv = new StringBuilder();
        for (int i = 1; i <= 5; i++) {
            if (!csv.isEmpty()) csv.append(',');           // isEmpty on CharSequence: Java 15
            csv.append(i * i);
        }
        System.out.println("loop: " + csv);

        StringJoiner joiner = new StringJoiner(", ", "[", "]");
        for (String lang : new String[]{"Java", "Kotlin", "Scala"}) joiner.add(lang);
        System.out.println("StringJoiner: " + joiner);

        // capacity grows automatically; pre-size when you know the final size
        StringBuilder big = new StringBuilder(1_000);
        System.out.println("capacity=" + big.capacity() + " length=" + big.length());
    }
}
