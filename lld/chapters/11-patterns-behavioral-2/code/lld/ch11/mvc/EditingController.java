package lld.ch11.mvc;

/** Interprets user input and updates the MODEL. It does not redraw anything itself. */
public final class EditingController implements TaskController {
    private final TaskModel model;
    private final TaskView view;

    public EditingController(TaskModel model, TaskView view) {
        this.model = model;
        this.view = view;
    }

    public void handle(String input) {
        try {
            if (input.startsWith("add ")) model.add(input.substring(4));
            else if (input.startsWith("done ")) model.complete(Integer.parseInt(input.substring(5).trim()));
            else view.showMessage("unknown command: " + input);
        } catch (IllegalArgumentException e) {        // NumberFormatException is a subclass
            view.showMessage("error: " + e.getMessage());
        }
    }
}
