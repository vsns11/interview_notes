package lld.ch11.di;

public final class SmsSender implements MessageSender {
    public void send(String to, String text) { System.out.println("  [sms] to " + to + ": " + text); }
}
