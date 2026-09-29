package lld.ch04.dip.infra;

import lld.ch04.dip.app.NotificationSender;

public class SmsSender implements NotificationSender {
    @Override
    public void send(String recipient, String message) {
        System.out.println("[sms -> " + recipient + "] " + message);
    }
}
