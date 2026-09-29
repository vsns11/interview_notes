package ch04.resilience;

/** A failure that is worth retrying (e.g. a timeout). */
public class TransientException extends RuntimeException {
    public TransientException(String message) { super(message); }
}
