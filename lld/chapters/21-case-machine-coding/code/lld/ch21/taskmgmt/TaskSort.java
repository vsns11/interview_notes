package lld.ch21.taskmgmt;

import java.time.LocalDate;
import java.util.Comparator;

/** Sorting strategies; each constant wraps a Comparator (Strategy pattern). */
public enum TaskSort {
    BY_DUE_DATE(Comparator.comparing(Task::dueDate, Comparator.nullsLast(Comparator.<LocalDate>naturalOrder()))),
    BY_PRIORITY_DESC(Comparator.comparing(Task::priority).reversed()),
    BY_ID(Comparator.comparingInt((Task t) -> Integer.parseInt(t.id().substring(1))));   // numeric: T2 before T10

    private final Comparator<Task> comparator;
    TaskSort(Comparator<Task> c) { this.comparator = c.thenComparing(Task::id); }
    public Comparator<Task> comparator() { return comparator; }
}
