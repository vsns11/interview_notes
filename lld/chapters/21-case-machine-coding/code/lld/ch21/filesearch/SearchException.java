package lld.ch21.filesearch;

/** Bad path, bad option or bad expression: a readable message, never a stack trace for the user. */
public class SearchException extends RuntimeException {
    public SearchException(String message) { super(message); }
}
