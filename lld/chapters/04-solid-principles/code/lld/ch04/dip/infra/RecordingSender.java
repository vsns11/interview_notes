package lld.ch04.dip.infra;

import java.util.ArrayList;
import java.util.List;
import lld.ch04.dip.app.NotificationSender;

/** A test double: proves NotificationService can be tested with no mail server. */
public class RecordingSender implements NotificationSender {
    private final List<String> sent = new ArrayList<>();

    @Override
    public void send(String recipient, String message) {
        sent.add(recipient + ":" + message);
    }

    public List<String> sent() {
        return List.copyOf(sent);
    }
}
