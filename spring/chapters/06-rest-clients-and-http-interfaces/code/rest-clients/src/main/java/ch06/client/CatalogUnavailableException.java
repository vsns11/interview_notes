package ch06.client;

/** Our exception type: callers should not depend on the HTTP library's exceptions. */
public class CatalogUnavailableException extends RuntimeException {
    public CatalogUnavailableException(String message) { super(message); }
}
