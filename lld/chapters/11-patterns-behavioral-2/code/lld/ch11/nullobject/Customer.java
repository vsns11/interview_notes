package lld.ch11.nullobject;

/** The abstraction both the real object and the Null Object implement. */
public interface Customer {
    String displayName();
    int discountPercent();
    void notify(String message);
    default boolean isGuest() { return false; }
}
