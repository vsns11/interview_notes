package lld.ch21.taskmgmt;

public class InvalidTransitionException extends RuntimeException {
    public InvalidTransitionException(String taskId, TaskStatus from, TaskStatus to) {
        super("Task " + taskId + " cannot move from " + from + " to " + to);
    }
}
