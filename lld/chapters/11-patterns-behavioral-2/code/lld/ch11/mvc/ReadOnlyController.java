package lld.ch11.mvc;

/** Alternative strategy: same view, different behaviour (e.g. a viewer role). */
public final class ReadOnlyController implements TaskController {
    private final TaskView view;

    public ReadOnlyController(TaskView view) { this.view = view; }

    public void handle(String input) { view.showMessage("read-only mode, ignored: " + input); }
}
