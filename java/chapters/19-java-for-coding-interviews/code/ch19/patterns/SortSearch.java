package ch19.patterns;

import java.util.*;

import static ch19.Check.*;

/** Sort and search: quicksort, quickselect (k-th largest), Dutch national flag, merge sort on a linked list. */
public class SortSearch {
    private static final Random RNG = new Random(42);          // fixed seed: same pivots every run

    /** Lomuto partition around a random pivot: returns the pivot's final index; smaller values end up left of it. */
    static int partition(int[] a, int lo, int hi) {
        swap(a, lo + RNG.nextInt(hi - lo + 1), hi);           // random pivot defeats sorted-input worst case
        int pivot = a[hi], store = lo;
        for (int i = lo; i < hi; i++) if (a[i] < pivot) swap(a, i, store++);
        swap(a, store, hi);
        return store;
    }

    static void quickSort(int[] a, int lo, int hi) {
        if (lo >= hi) return;
        int p = partition(a, lo, hi);
        quickSort(a, lo, p - 1);
        quickSort(a, p + 1, hi);
    }

    /** Quickselect: partition, then recurse into ONE side only. Average O(n), worst O(n^2). */
    static int kthLargest(int[] input, int k) {
        int[] a = input.clone();                               // don't reorder the caller's array
        int target = a.length - k;                             // k-th largest = index n-k in ascending order
        int lo = 0, hi = a.length - 1;
        while (true) {
            int p = partition(a, lo, hi);
            if (p == target) return a[p];
            if (p < target) lo = p + 1; else hi = p - 1;
        }
    }

    /** Dutch national flag: sort 0s, 1s and 2s in one pass with three regions. */
    static void sortColors(int[] a) {
        int low = 0, mid = 0, high = a.length - 1;             // [0,low)=0s  [low,mid)=1s  (high,end]=2s
        while (mid <= high) {
            if (a[mid] == 0) swap(a, low++, mid++);
            else if (a[mid] == 1) mid++;
            else swap(a, mid, high--);                         // don't advance mid: the swapped-in value is unchecked
        }
    }

    private static void swap(int[] a, int i, int j) { int t = a[i]; a[i] = a[j]; a[j] = t; }

    // ---------- merge sort on a singly linked list ----------
    static final class Node {
        int val; Node next;
        Node(int val, Node next) { this.val = val; this.next = next; }
    }

    /** Split at the middle with fast/slow, sort each half, merge. O(n log n), no random access needed. */
    static Node mergeSort(Node head) {
        if (head == null || head.next == null) return head;
        Node slow = head, fast = head.next;                   // fast starts one ahead so slow stops at the END of the left half
        while (fast != null && fast.next != null) { slow = slow.next; fast = fast.next.next; }
        Node right = slow.next;
        slow.next = null;                                      // cut the list in two
        return merge(mergeSort(head), mergeSort(right));
    }

    private static Node merge(Node a, Node b) {
        Node dummy = new Node(0, null), tail = dummy;
        while (a != null && b != null) {
            if (a.val <= b.val) { tail.next = a; a = a.next; }  // <= keeps it stable
            else { tail.next = b; b = b.next; }
            tail = tail.next;
        }
        tail.next = (a != null) ? a : b;
        return dummy.next;
    }

    static Node list(int... v) { Node h = null; for (int i = v.length - 1; i >= 0; i--) h = new Node(v[i], h); return h; }
    static List<Integer> values(Node h) { List<Integer> out = new ArrayList<>(); for (; h != null; h = h.next) out.add(h.val); return out; }

    public static void main(String[] args) {
        int[] a = {5, 2, 9, 1, 5, 6, 0, -3};
        quickSort(a, 0, a.length - 1);
        eq(a, new int[]{-3, 0, 1, 2, 5, 5, 6, 9}, "quickSort");
        int[] b = {3, 2, 1, 5, 6, 4};
        eq(kthLargest(b, 2), 5, "quickselect 2nd largest");
        eq(kthLargest(new int[]{3, 2, 3, 1, 2, 4, 5, 5, 6}, 4), 4, "quickselect with duplicates");
        eq(b, new int[]{3, 2, 1, 5, 6, 4}, "input left untouched");
        int[] colors = {2, 0, 2, 1, 1, 0};
        sortColors(colors);
        eq(colors, new int[]{0, 0, 1, 1, 2, 2}, "Dutch national flag");
        eq(values(mergeSort(list(4, 2, 1, 3))), List.of(1, 2, 3, 4), "merge sort list");
        eq(values(mergeSort(list(-1, 5, 3, 4, 0))), List.of(-1, 0, 3, 4, 5), "merge sort odd length");
        done();
    }
}
