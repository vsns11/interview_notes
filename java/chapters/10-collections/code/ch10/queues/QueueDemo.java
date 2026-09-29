package ch10.queues;

import java.util.*;

/** Queue, Deque (ArrayDeque as stack and queue) and PriorityQueue. */
public class QueueDemo {
    record Task(String name, int priority) {}

    public static void main(String[] args) {
        // FIFO queue: offer at the tail, poll from the head
        Queue<String> printer = new ArrayDeque<>();
        printer.offer("doc1"); printer.offer("doc2"); printer.offer("doc3");
        System.out.println("peek=" + printer.peek() + " poll=" + printer.poll() + " left=" + printer);

        // Two method families: exceptions vs special values on an empty queue
        Queue<String> empty = new ArrayDeque<>();
        System.out.println("empty.poll()=" + empty.poll() + " empty.peek()=" + empty.peek());
        try { empty.remove(); } catch (NoSuchElementException e) { System.out.println("empty.remove() -> NoSuchElementException"); }

        // LIFO stack with ArrayDeque (preferred over the legacy Stack class)
        Deque<Character> stack = new ArrayDeque<>();
        String expr = "{[()]}(";
        for (char c : expr.toCharArray()) {
            if ("([{".indexOf(c) >= 0) stack.push(c);
            else if (!stack.isEmpty() && "([{".indexOf(stack.peek()) == ")]}".indexOf(c)) stack.pop();
            else { stack.push('!'); break; }
        }
        System.out.println(expr + " balanced? " + stack.isEmpty() + " (left on stack: " + stack + ")");

        // ArrayDeque rejects null (null is its 'empty' signal)
        try { stack.push(null); } catch (NullPointerException e) { System.out.println("ArrayDeque.push(null) -> NPE"); }

        // PriorityQueue: a binary min-heap. poll() always returns the smallest.
        PriorityQueue<Integer> heap = new PriorityQueue<>(List.of(7, 3, 9, 1, 5));
        System.out.println("heap toString (NOT sorted): " + heap);
        StringBuilder out = new StringBuilder();
        while (!heap.isEmpty()) out.append(heap.poll()).append(' ');
        System.out.println("poll order: " + out);

        // Custom order: highest priority first, ties by name
        PriorityQueue<Task> jobs = new PriorityQueue<>(
                Comparator.comparingInt(Task::priority).reversed().thenComparing(Task::name));
        jobs.add(new Task("email", 1)); jobs.add(new Task("deploy", 5)); jobs.add(new Task("backup", 5)); jobs.add(new Task("lint", 2));
        while (!jobs.isEmpty()) System.out.print(jobs.poll().name() + " ");
        System.out.println();

        // Top-k largest with a size-k MIN-heap: O(n log k)
        int k = 3; PriorityQueue<Integer> topK = new PriorityQueue<>();
        for (int x : new int[]{15, 2, 99, 42, 8, 77, 23}) { topK.offer(x); if (topK.size() > k) topK.poll(); }
        List<Integer> result = new ArrayList<>(topK); result.sort(Comparator.reverseOrder());
        System.out.println("top-3 = " + result);
    }
}
