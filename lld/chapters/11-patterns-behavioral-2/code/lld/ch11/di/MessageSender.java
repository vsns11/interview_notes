package lld.ch11.di;

/** The abstraction the service depends on (Dependency Inversion). */
public interface MessageSender {
    void send(String to, String text);
}
