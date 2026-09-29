package ch10.lists;

import java.util.*;

/** ArrayList and LinkedList: index-based operations and the classic traps. */
public class ListDemo {
    public static void main(String[] args) {
        List<String> todo = new ArrayList<>(List.of("write", "test", "ship"));
        todo.add(1, "review");                 // insert at index 1, shifts the rest right
        todo.set(0, "design");                 // replace; returns the old value
        System.out.println(todo + " get(2)=" + todo.get(2) + " indexOf(ship)=" + todo.indexOf("ship"));

        // remove(int index) vs remove(Object o) -- the Integer trap
        List<Integer> nums = new ArrayList<>(List.of(10, 20, 30, 1));
        nums.remove(1);                        // index 1 -> removes 20
        nums.remove(Integer.valueOf(1));       // object 1 -> removes the value 1
        System.out.println("nums=" + nums);

        // subList is a VIEW into the original list
        List<String> firstTwo = todo.subList(0, 2);
        firstTwo.clear();                      // clears those two elements from todo!
        System.out.println("after subList.clear(): " + todo);

        // Arrays.asList: fixed-size, write-through view of the array
        String[] arr = {"a", "b", "c"};
        List<String> view = Arrays.asList(arr);
        view.set(0, "z");
        System.out.println("array now " + Arrays.toString(arr));
        try { view.add("d"); } catch (UnsupportedOperationException e) { System.out.println("asList.add -> UnsupportedOperationException"); }

        // LinkedList is a List AND a Deque (doubly linked nodes)
        LinkedList<String> ll = new LinkedList<>(List.of("b", "c"));
        ll.addFirst("a"); ll.addLast("d");
        System.out.println("linked=" + ll + " removeFirst=" + ll.removeFirst() + " peekLast=" + ll.peekLast());

        // Sorting a list in place, and List.sort(null) = natural order
        List<String> names = new ArrayList<>(List.of("mia", "Al", "zoe", "Bob", "ann"));
        names.sort(null);
        System.out.println("natural (uppercase first): " + names);
        names.sort(String.CASE_INSENSITIVE_ORDER);
        System.out.println("case-insensitive: " + names);

        // equals across implementations: same elements, same order -> equal
        System.out.println("ArrayList equals LinkedList? " + new ArrayList<>(ll).equals(new LinkedList<>(ll)));
    }
}
