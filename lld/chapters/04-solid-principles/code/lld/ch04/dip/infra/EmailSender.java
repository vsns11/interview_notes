package lld.ch04.dip.infra;

import lld.ch04.dip.app.NotificationSender;

/** Detail depends on the abstraction -- the arrow now points UP into app. */
public class EmailSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("[email -> " + recipient + "] " + message);
    }
}
