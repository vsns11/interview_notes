package lld.ch21.taskmgmt;

import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;

/** Facade for every use case. Holds no storage details and no printing. */
public class TaskService {
    private final TaskRepository tasks;
    private final Map<String, User> users = new ConcurrentHashMap<>();
    private final List<TaskEventListener> listeners = new CopyOnWriteArrayList<>();
    private final AtomicInteger seq = new AtomicInteger();
    private final Clock clock;

    public TaskService(TaskRepository tasks, Clock clock) {
        this.tasks = tasks;
        this.clock = clock;
    }

    public void addListener(TaskEventListener l) { listeners.add(l); }

    public User addUser(String id, String name) {
        User u = new User(id, name);
        if (users.putIfAbsent(id, u) != null) throw new IllegalArgumentException("User exists: " + id);
        return u;
    }

    public Task createTask(String creatorId, String title, String description, Priority priority, LocalDate due) {
        requireUser(creatorId);
        if (title == null || title.isBlank()) throw new IllegalArgumentException("Title is required");
        if (due != null && due.isBefore(today())) throw new IllegalArgumentException("Due date is in the past: " + due);
        Task t = new Task("T" + seq.incrementAndGet(), creatorId, title, description, priority, due);
        tasks.save(t);
        return t;
    }

    public void assign(String taskId, String assigneeId, String actorId) {
        requireUser(actorId);
        requireUser(assigneeId);
        Task t = get(taskId);
        t.assignTo(assigneeId, actorId, now());
        if (!assigneeId.equals(actorId)) fire(assigneeId, actorId + " assigned you " + t.id() + " '" + t.title() + "'");
    }

    public void changeStatus(String taskId, TaskStatus next, String actorId) {
        requireUser(actorId);
        Task t = get(taskId);
        t.changeStatus(next, actorId, now());
        if (!t.createdBy().equals(actorId)) fire(t.createdBy(), t.id() + " is now " + next);
    }

    public void update(String taskId, String title, Priority priority, LocalDate due, String actorId) {
        requireUser(actorId);
        if (title != null && title.isBlank()) throw new IllegalArgumentException("Title cannot be blank");
        if (due != null && due.isBefore(today())) throw new IllegalArgumentException("Due date is in the past: " + due);
        get(taskId).update(title, priority, due, actorId, now());
    }

    public void comment(String taskId, String authorId, String text) {
        requireUser(authorId);
        Task t = get(taskId);
        t.addComment(new Comment(authorId, text, now()));
        String other = authorId.equals(t.assigneeId()) ? t.createdBy() : t.assigneeId();
        if (other != null && !other.equals(authorId)) fire(other, authorId + " commented on " + t.id());
    }

    public boolean delete(String taskId) { return tasks.delete(taskId); }

    public List<Task> search(TaskFilter filter, TaskSort sort) {
        return tasks.findAll().stream().filter(filter).sorted(sort.comparator()).toList();
    }

    /** Reminder sweep: open tasks due within `days` days (or overdue). A scheduler would call this. */
    public int sendDueReminders(int days) {
        LocalDate limit = today().plusDays(days);
        List<Task> due = search(TaskFilter.open().and(TaskFilter.dueOnOrBefore(limit)), TaskSort.BY_DUE_DATE);
        for (Task t : due) {
            String who = t.assigneeId() != null ? t.assigneeId() : t.createdBy();
            String when = t.dueDate().isBefore(today()) ? "OVERDUE since " + t.dueDate() : "due " + t.dueDate();
            fire(who, "Reminder: " + t.id() + " '" + t.title() + "' is " + when);
        }
        return due.size();
    }

    public Task get(String taskId) {
        return tasks.findById(taskId).orElseThrow(() -> new NotFoundException("Task", taskId));
    }

    private void requireUser(String id) { if (!users.containsKey(id)) throw new NotFoundException("User", id); }
    private void fire(String to, String msg) { listeners.forEach(l -> l.onEvent(to, msg)); }
    private LocalDate today() { return LocalDate.now(clock); }
    private LocalDateTime now() { return LocalDateTime.now(clock); }
}
