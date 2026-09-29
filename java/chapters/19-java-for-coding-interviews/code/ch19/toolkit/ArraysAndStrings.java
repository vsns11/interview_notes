package ch19.toolkit;

import java.util.*;

/** Arrays, the Arrays class, String/StringBuilder and char arithmetic: the everyday interview toolkit. */
public class ArraysAndStrings {
    public static void main(String[] args) {
        // ---- arrays and java.util.Arrays ----
        int[] a = {5, 2, 9, 1};
        Arrays.sort(a);                                        // in place, ascending
        System.out.println("sorted      " + Arrays.toString(a));
        int[] part = Arrays.copyOfRange(a, 1, 3);              // [from, to) -> indexes 1..2
        System.out.println("copyOfRange " + Arrays.toString(part));
        int[] dist = new int[4];
        Arrays.fill(dist, Integer.MAX_VALUE);                  // "infinity" for shortest paths
        System.out.println("fill        " + Arrays.toString(dist));
        int[][] grid = new int[2][3];                          // 2 rows x 3 cols, all 0
        grid[1][2] = 7;
        System.out.println("grid        " + Arrays.deepToString(grid));
        System.out.println("equals      " + Arrays.equals(new int[]{1, 2}, new int[]{1, 2})
                + "  ==  " + (new int[]{1, 2} == new int[]{1, 2}));
        System.out.println("sum/max     " + Arrays.stream(a).sum() + " " + Arrays.stream(a).max().getAsInt());

        // ---- char arithmetic: a char is a 16-bit number ----
        String word = "banana";
        int[] freq = new int[26];
        for (char c : word.toCharArray()) freq[c - 'a']++;     // 'a'->0, 'b'->1, ...
        System.out.println("freq a,b,n  " + freq[0] + "," + freq[1] + "," + freq['n' - 'a']);
        char next = (char) ('a' + 2);                          // int arithmetic, cast back to char
        System.out.println("'a'+2       " + ('a' + 2) + " as char " + next);
        System.out.println("digit value " + ('7' - '0') + "  Character.getNumericValue " + Character.getNumericValue('7'));
        System.out.println("helpers     " + Character.isLetterOrDigit('_') + " " + Character.isDigit('4')
                + " " + Character.toLowerCase('Q'));

        // ---- String: immutable; build with StringBuilder ----
        String s = "  Hello, World  ";
        System.out.println("trim/strip  [" + s.strip() + "] lower=" + s.strip().toLowerCase());
        System.out.println("charAt/sub  " + "hello".charAt(1) + " " + "hello".substring(1, 3) + " " + "hello".indexOf('l'));
        System.out.println("split       " + Arrays.toString("a,b,,c".split(",")) + "  words="
                + Arrays.toString("  two   spaces ".trim().split("\\s+")));
        char[] cs = "dcba".toCharArray();
        Arrays.sort(cs);
        System.out.println("sorted chars " + new String(cs) + "  valueOf=" + String.valueOf(cs));

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 5; i++) sb.append(i).append(',');
        sb.setLength(sb.length() - 1);                         // drop the trailing comma
        System.out.println("builder     " + sb + "  reversed=" + new StringBuilder("abc").reverse());
        sb.setCharAt(0, 'X');
        sb.insert(0, '[').append(']');
        System.out.println("edited      " + sb + "  deleteCharAt(1)=" + new StringBuilder("abc").deleteCharAt(1));
        System.out.println("join/repeat " + String.join("-", List.of("a", "b", "c")) + " " + "ab".repeat(3));
        System.out.println("parse       " + (Integer.parseInt("-42") + 1) + " " + Integer.toBinaryString(10)
                + " " + Integer.parseInt("1010", 2));
    }
}
