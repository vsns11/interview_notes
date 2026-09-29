package ch07.tx;

/** A CHECKED exception: by default Spring does NOT roll back for it. */
public class FraudCheckException extends Exception {
    public FraudCheckException(String message) {
        super(message);
    }
}
