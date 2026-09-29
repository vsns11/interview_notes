package lld.ch21.taskmgmt;

import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

public class Main {
    public static void main(String[] args) {
        // Fixed clock => the demo prints the same thing on every run.
        Clock clock = Clock.fixed(Instant.parse("2026-09-28T09:00:00Z"), ZoneOffset.UTC);
        LocalDate today = LocalDate.now(clock);
        TaskService svc = new TaskService(new InMemoryTaskRepository(), clock);
        svc.addListener(new ConsoleNotifier());

        svc.addUser("alice", "Alice");
        svc.addUser("bob", "Bob");
        svc.addUser("carol", "Carol");

        System.out.println("== create & assign");
        Task t1 = svc.createTask("alice", "Fix login bug", "NPE on empty password", Priority.CRITICAL, today.plusDays(1));
        Task t2 = svc.createTask("alice", "Write release notes", "", Priority.LOW, today.plusDays(7));
        Task t3 = svc.createTask("bob", "Upgrade to Java 21", "", Priority.HIGH, today.plusDays(3));
        Task t4 = svc.createTask("carol", "Review PR #42", "", Priority.MEDIUM, today);
        svc.assign(t1.id(), "bob", "alice");
        svc.assign(t2.id(), "carol", "alice");
        svc.assign(t3.id(), "bob", "bob");

        System.out.println("== work on tasks");
        svc.changeStatus(t1.id(), TaskStatus.IN_PROGRESS, "bob");
        svc.comment(t1.id(), "bob", "Root cause found in PasswordValidator");
        svc.changeStatus(t1.id(), TaskStatus.DONE, "bob");
        svc.update(t3.id(), null, Priority.CRITICAL, null, "bob");

        System.out.println("== search: bob's open tasks, by priority");
        svc.search(TaskFilter.assignedTo("bob").and(TaskFilter.open()), TaskSort.BY_PRIORITY_DESC)
           .forEach(t -> System.out.println("  " + t));
        System.out.println("== search: priority >= MEDIUM due within 3 days, by due date");
        svc.search(TaskFilter.atLeast(Priority.MEDIUM).and(TaskFilter.dueOnOrBefore(today.plusDays(3))),
                   TaskSort.BY_DUE_DATE).forEach(t -> System.out.println("  " + t));

        System.out.println("== reminders (due within 3 days)");
        System.out.println("  reminders sent: " + svc.sendDueReminders(3));

        System.out.println("== history of " + t1.id());
        svc.get(t1.id()).history().forEach(a -> System.out.println("  " + a));

        System.out.println("== edge cases");
        attempt(() -> svc.changeStatus(t2.id(), TaskStatus.DONE, "carol"));   // TODO -> DONE skips IN_PROGRESS
        attempt(() -> svc.assign("T99", "bob", "alice"));
        attempt(() -> svc.assign(t2.id(), "dave", "alice"));
        attempt(() -> svc.createTask("alice", "Past task", "", Priority.LOW, today.minusDays(1)));
        svc.changeStatus(t4.id(), TaskStatus.CANCELLED, "carol");
        attempt(() -> svc.assign(t4.id(), "bob", "carol"));
        System.out.println("  deleted T2? " + svc.delete(t2.id()) + ", remaining=" + svc.search(TaskFilter.all(), TaskSort.BY_ID).size());
    }

    private static void attempt(Runnable r) {
        try { r.run(); System.out.println("  ok"); }
        catch (RuntimeException e) { System.out.println("  error: " + e.getMessage()); }
    }
}
