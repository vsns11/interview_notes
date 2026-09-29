package lld.ch11.mvc;

/** CONTROLLER as a STRATEGY: the view delegates "what does this input mean?" to it. */
public interface TaskController {
    void handle(String input);
}
