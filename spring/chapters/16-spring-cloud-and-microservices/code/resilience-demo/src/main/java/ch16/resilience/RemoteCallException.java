package ch16.resilience;

public class RemoteCallException extends RuntimeException {
    public RemoteCallException(String message) { super(message); }
}
