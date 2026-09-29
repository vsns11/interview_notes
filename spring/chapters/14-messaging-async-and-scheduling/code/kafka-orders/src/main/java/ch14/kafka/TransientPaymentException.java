package ch14.kafka;

/** "The payment provider is down right now" - worth retrying. */
public class TransientPaymentException extends RuntimeException {
    public TransientPaymentException(String message) { super(message); }
}
