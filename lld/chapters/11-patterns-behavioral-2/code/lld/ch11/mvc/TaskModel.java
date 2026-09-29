package lld.ch11.mvc;

import java.util.ArrayList;
import java.util.List;

/** MODEL: state + business rules. Knows NOTHING about views or controllers, only listeners. */
public final class TaskModel {
    private final List<Task> tasks = new ArrayList<>();
    private final List<ModelListener> listeners = new ArrayList<>();
    private int nextId = 1;

    public void addListener(ModelListener l) { listeners.add(l); }

    public List<Task> tasks() { return List.copyOf(tasks); }

    public void add(String title) {
        if (title.isBlank()) throw new IllegalArgumentException("title must not be blank");
        tasks.add(new Task(nextId++, title, false));
        fireChanged();
    }

    public void complete(int id) {
        for (int i = 0; i < tasks.size(); i++) {
            Task t = tasks.get(i);
            if (t.id() == id) {
                tasks.set(i, new Task(t.id(), t.title(), true));
                fireChanged();
                return;
            }
        }
        throw new IllegalArgumentException("no task " + id);
    }

    private void fireChanged() { listeners.forEach(l -> l.modelChanged(this)); }
}
