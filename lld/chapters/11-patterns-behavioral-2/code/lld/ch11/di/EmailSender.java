package lld.ch11.di;

public final class EmailSender implements MessageSender {
    public void send(String to, String text) { System.out.println("  [smtp] to " + to + ": " + text); }
}
