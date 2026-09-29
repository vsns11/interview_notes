package lld.ch21.taskmgmt;

/** Observer: anything that wants to hear about task events (email, Slack, push...). */
public interface TaskEventListener {
    void onEvent(String recipientId, String message);
}
