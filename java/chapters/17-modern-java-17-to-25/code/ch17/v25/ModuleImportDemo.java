package ch17.v25;

import module java.base;           // JEP 511 (Java 25): imports every package exported by java.base

/** One import line gives List, Map, Stream, Collectors, Path, LocalDate, ... */
public class ModuleImportDemo {
    public static void main(String[] args) {
        List<String> words = List.of("modules", "import", "java", "base");
        Map<Integer, List<String>> byLength = words.stream().collect(Collectors.groupingBy(String::length));
        IO.println("by length : " + new TreeMap<>(byLength));                 // java.lang.IO: Java 25 (JEP 512)
        IO.println("a path    : " + Path.of("docs", "ch17.md"));
        IO.println("a date    : " + LocalDate.of(2025, 9, 16).getDayOfWeek()); // Java 25 GA day
    }
}
