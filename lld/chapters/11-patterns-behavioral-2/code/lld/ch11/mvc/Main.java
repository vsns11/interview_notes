package lld.ch11.mvc;

public class Main {
    public static void main(String[] args) {
        TaskModel model = new TaskModel();
        TaskView view = new TaskView();
        model.addListener(view);                                   // Observer: model -> view
        view.setController(new EditingController(model, view));  // Strategy: view -> controller

        view.userTyped("add write chapter 11");
        view.userTyped("add review diagrams");
        view.userTyped("done 1");
        view.userTyped("done 42");

        System.out.println("-- switching the view to a read-only controller --");
        view.setController(new ReadOnlyController(view));
        view.userTyped("add sneaky task");
    }
}
