package ch19.toolkit;

import java.util.*;

/** ArrayDeque as stack and queue, PriorityQueue (min and max), TreeMap/TreeSet navigation. */
public class DequesHeapsTrees {
    public static void main(String[] args) {
        // ---- ArrayDeque as a STACK (LIFO): push / pop / peek work on the head ----
        Deque<Integer> stack = new ArrayDeque<>();
        stack.push(1); stack.push(2); stack.push(3);
        System.out.println("stack peek=" + stack.peek() + " pop=" + stack.pop() + " iterates " + stack);

        // Legacy java.util.Stack iterates bottom-to-top: a trap when you print or stream it
        Stack<Integer> legacy = new Stack<>();
        legacy.push(1); legacy.push(2); legacy.push(3);
        System.out.println("legacy Stack iterates " + legacy + " and allows get(0)=" + legacy.get(0));

        // ---- ArrayDeque as a QUEUE (FIFO): offer at tail, poll from head ----
        Deque<String> queue = new ArrayDeque<>();
        queue.offer("a"); queue.offer("b"); queue.offer("c");
        System.out.println("queue poll=" + queue.poll() + " peek=" + queue.peek() + " left " + queue);
        System.out.println("empty poll -> " + new ArrayDeque<Integer>().poll() + " (pop() would throw)");

        // ---- PriorityQueue: a binary MIN-heap by default ----
        PriorityQueue<Integer> min = new PriorityQueue<>(List.of(5, 1, 8, 3));
        PriorityQueue<Integer> max = new PriorityQueue<>(Comparator.reverseOrder());
        max.addAll(List.of(5, 1, 8, 3));
        System.out.println("min.peek=" + min.peek() + " max.peek=" + max.peek()
                + "  toString is heap order, not sorted: " + min);
        StringBuilder drained = new StringBuilder();
        while (!min.isEmpty()) drained.append(min.poll()).append(' ');
        System.out.println("poll order " + drained.toString().trim());

        // heap of int[] {distance, node}: compare by index 0 without overflow
        PriorityQueue<int[]> pq = new PriorityQueue<>(Comparator.comparingInt(x -> x[0]));
        pq.offer(new int[]{7, 1}); pq.offer(new int[]{2, 9}); pq.offer(new int[]{4, 3});
        System.out.println("int[] heap first " + Arrays.toString(pq.poll()));

        // ---- TreeMap / TreeSet: sorted, O(log n) navigation ----
        TreeMap<Integer, String> prices = new TreeMap<>(Map.of(10, "ten", 20, "twenty", 30, "thirty"));
        System.out.println("floorKey(25)=" + prices.floorKey(25) + " ceilingKey(25)=" + prices.ceilingKey(25)
                + " lowerKey(20)=" + prices.lowerKey(20) + " higherKey(30)=" + prices.higherKey(30));
        System.out.println("first=" + prices.firstKey() + " last=" + prices.lastEntry()
                + " headMap(20)=" + prices.headMap(20) + " tailMap(20)=" + prices.tailMap(20));
        TreeSet<Integer> set = new TreeSet<>(List.of(1, 4, 9, 16));
        System.out.println("TreeSet floor(10)=" + set.floor(10) + " ceiling(10)=" + set.ceiling(10)
                + " pollFirst=" + set.pollFirst() + " descending=" + set.descendingSet());
    }
}
