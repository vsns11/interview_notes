package ch03.loops;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;

/** Mistakes that compile fine but misbehave. */
public class LoopPitfalls {
    public static void main(String[] args) {
        int[] a = {10, 20, 30};
        try {
            for (int i = 0; i <= a.length; i++) System.out.print(a[i] + " ");   // off-by-one: <= should be <
        } catch (ArrayIndexOutOfBoundsException e) {
            System.out.println("\noff-by-one -> " + e.getMessage());
        }

        List<Integer> nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        try {
            for (Integer x : nums) if (x % 2 == 0) nums.remove(x);   // structural change during for-each
        } catch (ConcurrentModificationException e) {
            System.out.println("remove in for-each -> ConcurrentModificationException");
        }
        nums = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        for (Iterator<Integer> it = nums.iterator(); it.hasNext(); ) {
            if (it.next() % 2 == 0) it.remove();                       // fix 1: Iterator.remove
        }
        System.out.println("after Iterator.remove: " + nums);
        List<Integer> more = new ArrayList<>(List.of(1, 2, 3, 4, 5, 6));
        more.removeIf(x -> x % 2 == 0);                                // fix 2: removeIf (Java 8)
        System.out.println("after removeIf: " + more);

        int count = 0;
        for (double d = 0.0; d != 1.0 && count < 20; d += 0.1) count++; // d never equals 1.0 exactly
        System.out.println("double counter ran " + count + " times (guard stopped it)");

        int[] values = {1, 2, 3};
        for (int v : values) v *= 10;                  // changes the copy, not the array
        System.out.println("values after 'v *= 10' in for-each: " + java.util.Arrays.toString(values));

        int total = 0;
        for (int i = 0; i < 5; i++); { total++; }      // stray ';' = empty loop body
        System.out.println("stray semicolon: block ran " + total + " time(s)");
    }
}
