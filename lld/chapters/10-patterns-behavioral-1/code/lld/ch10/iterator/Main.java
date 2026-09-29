package lld.ch10.iterator;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.Iterator;
import java.util.List;
import java.util.Spliterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== 1. Paged remote results behind a plain for-each ===");
        SearchApi api = new SearchApi();
        for (String r : new PagedSearchResults(api)) {
            System.out.println("  loop sees " + r);
        }
        System.out.println("  5 items, " + api.calls() + " network calls (the last one only discovers the end)");

        System.out.println("\n=== 2. Two independent iterators over one tree ===");
        BinarySearchTree<Integer> tree = new BinarySearchTree<>();
        for (int v : new int[] {50, 30, 70, 20, 40, 60, 80}) tree.insert(v);
        Iterator<Integer> a = tree.iterator();
        Iterator<Integer> b = tree.iterator();
        System.out.println("  a: " + a.next() + ", " + a.next() + ", " + a.next());
        System.out.println("  b: " + b.next() + "   <- b starts from the beginning: position lives in the iterator");
        StringBuilder all = new StringBuilder();
        for (int v : tree) all.append(v).append(' ');
        System.out.println("  in-order: " + all.toString().trim());

        System.out.println("\n=== 3. External vs internal iteration ===");
        List<String> names = List.of("ann", "bob", "cy");
        Iterator<String> it = names.iterator();                // external: the CLIENT drives
        while (it.hasNext()) {
            String n = it.next();
            if (n.equals("bob")) { System.out.println("  external: found bob, stopping early"); break; }
        }
        names.forEach(n -> System.out.println("  internal: " + n)); // internal: the COLLECTION drives

        System.out.println("\n=== 4. Fail-fast vs fail-safe ===");
        List<String> tags = new ArrayList<>(List.of("java", "tmp", "lld", "tmp"));
        try {
            for (String t : tags) {
                if (t.equals("tmp")) tags.remove(t);           // structural change behind the iterator's back
            }
        } catch (ConcurrentModificationException e) {
            System.out.println("  fail-fast: ConcurrentModificationException");
        }
        List<String> quiet = new ArrayList<>(List.of("java", "tmp", "lld"));
        List<String> visited = new ArrayList<>();
        for (String t : quiet) {
            visited.add(t);
            if (t.equals("tmp")) quiet.remove(t);              // removes the SECOND-TO-LAST element
        }
        System.out.println("  best-effort only: no exception, visited " + visited + ", 'lld' silently skipped");
        tags = new ArrayList<>(List.of("java", "tmp", "lld", "tmp"));
        for (Iterator<String> i = tags.iterator(); i.hasNext(); ) {
            if (i.next().equals("tmp")) i.remove();            // the safe way: remove through the iterator
        }
        System.out.println("  after Iterator.remove: " + tags);
        List<String> cow = new CopyOnWriteArrayList<>(List.of("x", "y"));
        for (String s : cow) cow.add(s + "!");                  // iterates a snapshot: no exception
        System.out.println("  fail-safe CopyOnWriteArrayList: " + cow);

        System.out.println("\n=== 5. Spliterator: an iterator that can split for parallel work ===");
        List<Integer> nums = List.of(1, 2, 3, 4, 5, 6, 7, 8);
        Spliterator<Integer> right = nums.spliterator();
        Spliterator<Integer> left = right.trySplit();           // hands off (roughly) the first half
        System.out.println("  SIZED=" + right.hasCharacteristics(Spliterator.SIZED)
                + " ORDERED=" + right.hasCharacteristics(Spliterator.ORDERED));
        StringBuilder l = new StringBuilder(), r = new StringBuilder();
        left.forEachRemaining(x -> l.append(x).append(' '));
        right.forEachRemaining(x -> r.append(x).append(' '));
        System.out.println("  left half:  " + l.toString().trim());
        System.out.println("  right half: " + r.toString().trim());
        System.out.println("  parallel stream sum = " + nums.parallelStream().mapToInt(Integer::intValue).sum());
    }
}
