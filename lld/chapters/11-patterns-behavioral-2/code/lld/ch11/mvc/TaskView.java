package lld.ch11.mvc;

import java.util.List;
import java.util.stream.Collectors;

/** VIEW: observes the model, renders it (composite), forwards user input to its controller (strategy). */
public final class TaskView implements ModelListener {
    private TaskController controller;
    private final Widget root;
    private TaskModel lastSeen;

    public TaskView() {
        root = new Widget.Panel("My Tasks", List.of(
            new Widget.Label(() -> lastSeen == null ? "(empty)" : lastSeen.tasks().stream()
                .map(t -> (t.done() ? "[x] " : "[ ] ") + t.id() + ". " + t.title())
                .collect(Collectors.joining("\n    "))),
            new Widget.Panel("Summary", List.of(
                new Widget.Label(() -> lastSeen == null ? "0 open" :
                    lastSeen.tasks().stream().filter(t -> !t.done()).count() + " open")))));
    }

    public void setController(TaskController controller) { this.controller = controller; }

    /** A user action arrives at the view... and is handed straight to the controller. */
    public void userTyped(String input) {
        System.out.println("> " + input);
        controller.handle(input);
    }

    public void showMessage(String msg) { System.out.println("  (" + msg + ")"); }

    @Override
    public void modelChanged(TaskModel model) {     // Observer callback: re-render from the model
        lastSeen = model;
        StringBuilder out = new StringBuilder();
        root.render(out, "  ");
        System.out.print(out);
    }
}
