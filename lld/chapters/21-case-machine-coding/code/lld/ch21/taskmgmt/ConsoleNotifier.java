package lld.ch21.taskmgmt;

public class ConsoleNotifier implements TaskEventListener {
    @Override public void onEvent(String recipientId, String message) {
        System.out.println("  [notify " + recipientId + "] " + message);
    }
}
