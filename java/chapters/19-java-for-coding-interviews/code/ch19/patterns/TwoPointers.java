package ch19.patterns;

import static ch19.Check.*;

/** Two pointers: opposite ends (sorted input), read/write pointers (in place), fast/slow (linked lists). */
public class TwoPointers {
    /** Indexes of two numbers in a SORTED array that add up to target, or {-1,-1}. O(n) time, O(1) space. */
    static int[] pairWithSum(int[] sorted, int target) {
        int left = 0, right = sorted.length - 1;
        while (left < right) {
            int sum = sorted[left] + sorted[right];
            if (sum == target) return new int[]{left, right};
            if (sum < target) left++;          // need a bigger sum
            else right--;                      // need a smaller sum
        }
        return new int[]{-1, -1};
    }

    /** Removes duplicates in place from a sorted array; returns the new logical length. */
    static int dedupe(int[] sorted) {
        if (sorted.length == 0) return 0;
        int write = 1;                         // sorted[0..write) holds the unique values
        for (int read = 1; read < sorted.length; read++) {
            if (sorted[read] != sorted[write - 1]) sorted[write++] = sorted[read];
        }
        return write;
    }

    /** Palindrome check that ignores case and non-alphanumeric characters. */
    static boolean isPalindrome(String s) {
        int i = 0, j = s.length() - 1;
        while (i < j) {
            char a = s.charAt(i), b = s.charAt(j);
            if (!Character.isLetterOrDigit(a)) { i++; continue; }
            if (!Character.isLetterOrDigit(b)) { j--; continue; }
            if (Character.toLowerCase(a) != Character.toLowerCase(b)) return false;
            i++; j--;
        }
        return true;
    }

    // ---- fast/slow pointers on a linked list ----
    static final class ListNode {
        int val; ListNode next;
        ListNode(int val) { this.val = val; }
    }

    static ListNode build(int... values) {
        ListNode dummy = new ListNode(0), tail = dummy;
        for (int v : values) { tail.next = new ListNode(v); tail = tail.next; }
        return dummy.next;
    }

    /** Middle node (second middle for even length): slow moves 1, fast moves 2. */
    static int middle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        return slow.val;
    }

    /** Floyd's cycle detection: if fast ever meets slow there is a cycle. */
    static boolean hasCycle(ListNode head) {
        ListNode slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) return true;     // reference comparison on purpose
        }
        return false;
    }

    /** Reverse a list iteratively with three pointers: prev, cur, next. */
    static ListNode reverse(ListNode head) {
        ListNode prev = null, cur = head;
        while (cur != null) {
            ListNode next = cur.next;
            cur.next = prev;
            prev = cur;
            cur = next;
        }
        return prev;
    }

    static String show(ListNode n) {
        StringBuilder sb = new StringBuilder();
        for (; n != null; n = n.next) sb.append(n.val).append(n.next == null ? "" : "->");
        return sb.toString();
    }

    public static void main(String[] args) {
        eq(pairWithSum(new int[]{1, 2, 4, 7, 11}, 15), new int[]{2, 4}, "pairWithSum 15");
        eq(pairWithSum(new int[]{1, 2}, 10), new int[]{-1, -1}, "pairWithSum none");
        int[] d = {1, 1, 2, 3, 3, 3, 4};
        int len = dedupe(d);
        eq(java.util.Arrays.copyOf(d, len), new int[]{1, 2, 3, 4}, "dedupe");
        isTrue(isPalindrome("A man, a plan, a canal: Panama"), "palindrome yes");
        isTrue(!isPalindrome("race a car"), "palindrome no");

        eq(middle(build(1, 2, 3, 4, 5)), 3, "middle odd");
        eq(middle(build(1, 2, 3, 4)), 3, "middle even");
        ListNode loop = build(1, 2, 3, 4);
        loop.next.next.next.next = loop.next;  // 4 -> 2 makes a cycle
        isTrue(hasCycle(loop), "cycle yes");
        isTrue(!hasCycle(build(1, 2, 3)), "cycle no");
        eq(show(reverse(build(1, 2, 3))), "3->2->1", "reverse");
        done();
    }
}
