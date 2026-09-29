package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Linked lists and fast/slow pointers: k-th from end, intersection, palindrome, cycle start, happy number. */
public class LinkedLists {
    static final class Node {
        int val; Node next;
        Node(int val) { this.val = val; }
    }

    static Node of(int... values) {                // build a list: of(1, 2, 3) -> 1->2->3
        Node dummy = new Node(0), tail = dummy;
        for (int v : values) { tail.next = new Node(v); tail = tail.next; }
        return dummy.next;
    }

    static String show(Node head) {
        StringJoiner sj = new StringJoiner("->");
        for (Node n = head; n != null; n = n.next) sj.add(String.valueOf(n.val));
        return sj.toString();
    }

    /** Remove the k-th node from the end in one pass: lead runs k nodes ahead of trail. */
    static Node removeKthFromEnd(Node head, int k) {
        Node dummy = new Node(0);                  // a dummy in front makes "remove the head" a normal case
        dummy.next = head;
        Node lead = dummy, trail = dummy;
        for (int i = 0; i < k; i++) lead = lead.next;          // open a gap of k
        while (lead.next != null) { lead = lead.next; trail = trail.next; }
        trail.next = trail.next.next;              // trail stops just BEFORE the node to delete
        return dummy.next;
    }

    /** First shared node of two lists, or null. Each pointer walks A then B, so both travel lenA + lenB. */
    static Node intersection(Node a, Node b) {
        Node p = a, q = b;
        while (p != q) {                           // reference comparison: same node object, not same value
            p = (p == null) ? b : p.next;
            q = (q == null) ? a : q.next;
        }
        return p;                                  // the meeting node, or null when both reach the end together
    }

    static Node reverse(Node head) {
        Node prev = null, cur = head;
        while (cur != null) { Node next = cur.next; cur.next = prev; prev = cur; cur = next; }
        return prev;
    }

    /** Palindrome in O(1) extra space: find the middle, reverse the second half, compare, restore. */
    static boolean isPalindrome(Node head) {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        Node secondHalf = reverse(slow);
        boolean ok = true;
        for (Node p = head, q = secondHalf; q != null; p = p.next, q = q.next)
            if (p.val != q.val) { ok = false; break; }
        reverse(secondHalf);                       // put the caller's list back the way it was
        return ok;
    }

    /** Floyd: after slow and fast meet, a pointer from the head and one from the meeting point meet at the cycle start. */
    static Node cycleStart(Node head) {
        Node slow = head, fast = head;
        while (fast != null && fast.next != null) {
            slow = slow.next; fast = fast.next.next;
            if (slow == fast) {
                Node p = head;
                while (p != slow) { p = p.next; slow = slow.next; }
                return p;
            }
        }
        return null;
    }

    /** Happy number: fast/slow on the sequence n -> sum of squares of digits. It either reaches 1 or loops. */
    static boolean isHappy(int n) {
        int slow = n, fast = next(n);
        while (fast != 1 && slow != fast) { slow = next(slow); fast = next(next(fast)); }
        return fast == 1;
    }

    private static int next(int n) {
        int sum = 0;
        while (n > 0) { int d = n % 10; sum += d * d; n /= 10; }
        return sum;
    }

    public static void main(String[] args) {
        eq(show(removeKthFromEnd(of(1, 2, 3, 4, 5), 2)), "1->2->3->5", "remove 2nd from end");
        eq(show(removeKthFromEnd(of(1, 2), 2)), "2", "remove the head");

        Node shared = of(8, 9);
        Node a = of(1, 2); a.next.next = shared;             // 1->2->8->9
        Node b = of(5, 6, 7); b.next.next.next = shared;     // 5->6->7->8->9
        eq(intersection(a, b).val, 8, "intersection node");
        eq(intersection(of(1, 2), of(3)), null, "no intersection");

        Node pal = of(1, 2, 3, 2, 1);
        eq(isPalindrome(pal), true, "palindrome odd");
        eq(show(pal), "1->2->3->2->1", "list restored after palindrome check");
        eq(isPalindrome(of(1, 2, 2, 1)), true, "palindrome even");
        eq(isPalindrome(of(1, 2, 3)), false, "not a palindrome");

        Node cyc = of(3, 2, 0, -4);
        cyc.next.next.next.next = cyc.next;                   // -4 points back to 2
        eq(cycleStart(cyc).val, 2, "cycle starts at 2");
        eq(cycleStart(of(1, 2, 3)), null, "no cycle");

        eq(isHappy(19), true, "19 is happy");
        eq(isHappy(2), false, "2 is not happy");
        done();
    }
}
