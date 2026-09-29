package lld.ch21.taskmgmt;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryTaskRepository implements TaskRepository {
    private final ConcurrentHashMap<String, Task> tasks = new ConcurrentHashMap<>();

    @Override public void save(Task task) { tasks.put(task.id(), task); }
    @Override public Optional<Task> findById(String id) { return Optional.ofNullable(tasks.get(id)); }
    @Override public Collection<Task> findAll() { return List.copyOf(tasks.values()); }
    @Override public boolean delete(String id) { return tasks.remove(id) != null; }
}
