package ch10.sorting;

import java.util.*;

/** Comparable (natural order) vs Comparator (any order), and comparator building blocks. */
public class SortingDemo {
    /** Natural order of an Employee = by id. compareTo returns negative / zero / positive. */
    record Employee(int id, String name, String dept, double salary) implements Comparable<Employee> {
        @Override public int compareTo(Employee o) { return Integer.compare(id, o.id); }
    }

    public static void main(String[] args) {
        List<Employee> staff = new ArrayList<>(List.of(
                new Employee(3, "Cy", "ENG", 120_000), new Employee(1, "Ana", "OPS", 90_000),
                new Employee(4, "Dee", "ENG", 120_000), new Employee(2, "Bo", "ENG", 150_000)));

        Collections.sort(staff);                                   // uses compareTo
        System.out.println("by id:      " + names(staff));

        staff.sort(Comparator.comparing(Employee::name).reversed());
        System.out.println("name desc:  " + names(staff));

        // Multi-key: dept asc, then salary desc, then name asc
        staff.sort(Comparator.comparing(Employee::dept)
                .thenComparing(Employee::salary, Comparator.reverseOrder())
                .thenComparing(Employee::name));
        System.out.println("dept,salary desc,name: " + names(staff));

        // Primitive-key comparators avoid boxing
        staff.sort(Comparator.comparingDouble(Employee::salary));
        System.out.println("salary asc: " + names(staff));

        // Nulls: nullsFirst / nullsLast wrap another comparator
        List<String> cities = new ArrayList<>(Arrays.asList("Oslo", null, "Accra", "lima"));
        cities.sort(Comparator.nullsLast(String.CASE_INSENSITIVE_ORDER));
        System.out.println("nullsLast, case-insensitive: " + cities);

        // Never subtract to compare: overflow flips the sign
        Comparator<Integer> bad = (a, b) -> a - b;
        System.out.println("bad.compare(MIN_VALUE, 1) = " + bad.compare(Integer.MIN_VALUE, 1)
                + " (positive, so MIN_VALUE looks BIGGER), Integer.compare = " + Integer.compare(Integer.MIN_VALUE, 1));

        // Comparator inconsistent with equals: a TreeSet treats compare()==0 as a duplicate
        Set<String> byLength = new TreeSet<>(Comparator.comparingInt(String::length));
        byLength.addAll(List.of("kiwi", "pear", "fig", "plum", "banana"));
        System.out.println("TreeSet by length keeps only " + byLength + "  (pear and plum were dropped!)");

        // Sort stability: equal keys keep their previous relative order (List.sort is a stable merge sort)
        List<Employee> s2 = new ArrayList<>(staff);
        s2.sort(Comparator.comparing(Employee::name));
        s2.sort(Comparator.comparing(Employee::dept));
        System.out.println("stable (dept, then previous name order): " + names(s2));
    }

    static List<String> names(List<Employee> l) { return l.stream().map(Employee::name).toList(); }
}
