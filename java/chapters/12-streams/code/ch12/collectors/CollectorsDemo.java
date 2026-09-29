package ch12.collectors;

import java.util.*;
import java.util.function.Function;
import java.util.stream.*;
import static java.util.stream.Collectors.*;

/** Collectors: turning a stream into collections, maps, strings and statistics. */
public class CollectorsDemo {
    record Employee(String name, String dept, String city, double salary, List<String> skills) {}

    static final List<Employee> STAFF = List.of(
            new Employee("Ana", "ENG", "Lisbon", 120_000, List.of("java", "sql")),
            new Employee("Bo", "ENG", "Berlin", 150_000, List.of("java", "k8s")),
            new Employee("Cy", "OPS", "Lisbon", 90_000, List.of("k8s", "bash")),
            new Employee("Dee", "SALES", "Berlin", 80_000, List.of("excel")),
            new Employee("Eli", "ENG", "Lisbon", 110_000, List.of("go", "sql")));

    public static void main(String[] args) {
        // joining
        System.out.println("joining          " + STAFF.stream().map(Employee::name).collect(joining(", ", "[", "]")));

        // toSet, toCollection
        System.out.println("toSet(TreeSet)   " + STAFF.stream().map(Employee::city).collect(toCollection(TreeSet::new)));

        // toMap: key mapper, value mapper, (merge function), (map factory)
        Map<String, Double> salaryByName = STAFF.stream().collect(toMap(Employee::name, Employee::salary));
        System.out.println("toMap            " + new TreeMap<>(salaryByName));
        try {
            STAFF.stream().collect(toMap(Employee::dept, Employee::name));      // duplicate key ENG!
        } catch (IllegalStateException e) { System.out.println("toMap dup key -> IllegalStateException: " + e.getMessage()); }
        Map<String, String> namesByDept = STAFF.stream()
                .collect(toMap(Employee::dept, Employee::name, (a, b) -> a + "+" + b, TreeMap::new));
        System.out.println("toMap merge      " + namesByDept);
        Map<String, Employee> byName = STAFF.stream().collect(toMap(Employee::name, Function.identity()));
        System.out.println("toMap identity   Bo -> " + byName.get("Bo").dept());

        // groupingBy: classifier -> List by default
        Map<String, List<String>> byDept = STAFF.stream()
                .collect(groupingBy(Employee::dept, TreeMap::new, mapping(Employee::name, toList())));
        System.out.println("groupingBy       " + byDept);
        System.out.println("  counting       " + STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new, counting())));
        System.out.println("  averaging      " + STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new, averagingDouble(Employee::salary))));
        System.out.println("  summing        " + STAFF.stream().collect(groupingBy(Employee::city, TreeMap::new, summingDouble(Employee::salary))));
        System.out.println("  maxBy          " + STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new,
                collectingAndThen(maxBy(Comparator.comparingDouble(Employee::salary)), o -> o.orElseThrow().name()))));
        System.out.println("  multi-level    " + STAFF.stream().collect(groupingBy(Employee::city, TreeMap::new,
                groupingBy(Employee::dept, TreeMap::new, counting()))));
        System.out.println("  filtering(9)   " + STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new,
                filtering(e -> e.salary() > 100_000, mapping(Employee::name, toList())))));
        System.out.println("  flatMapping(9) " + STAFF.stream().collect(groupingBy(Employee::dept, TreeMap::new,
                flatMapping(e -> e.skills().stream(), toCollection(TreeSet::new)))));

        // partitioningBy: always exactly two keys, false and true
        Map<Boolean, List<String>> highPaid = STAFF.stream()
                .collect(partitioningBy(e -> e.salary() >= 110_000, mapping(Employee::name, toList())));
        System.out.println("partitioningBy   " + highPaid);
        System.out.println("partition empty  " + Stream.<Employee>empty().collect(partitioningBy(e -> true)));

        // statistics
        DoubleSummaryStatistics stats = STAFF.stream().collect(summarizingDouble(Employee::salary));
        System.out.printf("summarizing      count=%d min=%.0f max=%.0f avg=%.0f sum=%.0f%n",
                stats.getCount(), stats.getMin(), stats.getMax(), stats.getAverage(), stats.getSum());

        // teeing (Java 12): two collectors at once, then merge their results
        String range = STAFF.stream().collect(teeing(
                minBy(Comparator.comparingDouble(Employee::salary)),
                maxBy(Comparator.comparingDouble(Employee::salary)),
                (min, max) -> min.orElseThrow().name() + ".." + max.orElseThrow().name()));
        System.out.println("teeing           lowest..highest paid = " + range);

        // collectingAndThen: post-process, e.g. make it unmodifiable
        List<String> frozen = STAFF.stream().map(Employee::name).collect(collectingAndThen(toList(), Collections::unmodifiableList));
        System.out.println("collectingAndThen " + frozen.getClass().getSimpleName());

        // reducing and a hand-written Collector via Collector.of
        System.out.println("reducing         " + STAFF.stream().map(Employee::salary).collect(reducing(0.0, Double::sum)));
        Collector<String, StringJoiner, String> pipes = Collector.of(
                () -> new StringJoiner("|"), StringJoiner::add, StringJoiner::merge, StringJoiner::toString);
        System.out.println("Collector.of     " + STAFF.stream().map(Employee::name).collect(pipes));
    }
}
