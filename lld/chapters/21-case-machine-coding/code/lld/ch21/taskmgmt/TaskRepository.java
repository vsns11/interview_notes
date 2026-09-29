package lld.ch21.taskmgmt;

import java.util.Collection;
import java.util.Optional;

/** Storage contract. The service depends on this, not on a HashMap (Dependency Inversion). */
public interface TaskRepository {
    void save(Task task);
    Optional<Task> findById(String id);
    Collection<Task> findAll();
    boolean delete(String id);
}
