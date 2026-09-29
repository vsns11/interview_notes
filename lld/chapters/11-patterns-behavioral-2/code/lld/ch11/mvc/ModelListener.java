package lld.ch11.mvc;

/** OBSERVER interface between model and views. */
public interface ModelListener {
    void modelChanged(TaskModel model);
}
