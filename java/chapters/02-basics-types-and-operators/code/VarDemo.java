package ch02.types;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Local-variable type inference with var (Java 10). */
public class VarDemo {
    public static void main(String[] args) {
        var count = 10;                          // int
        var ratio = 10 / 4.0;                    // double
        var name = "Ada";                        // String
        var scores = new HashMap<String, List<Integer>>();   // HashMap<String, List<Integer>>
        scores.put(name, new ArrayList<>(List.of(90, 85)));
        for (var entry : scores.entrySet()) {    // Map.Entry<String, List<Integer>>
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        // count = "ten";                        // compile error: the type is fixed at int
        var items = new ArrayList<>();           // careful: ArrayList<Object>
        items.add(1); items.add("two");
        System.out.println(count + " " + ratio + " " + items);
        Map<String, Integer> declared = Map.of("a", 1); // explicit type still fine
        System.out.println(declared);
    }
}
