package lld.ch11.nullobject;

/**
 * NULL OBJECT: a real, do-nothing implementation with neutral values.
 * Stateless, so one shared instance is enough (an enum singleton).
 */
public enum GuestCustomer implements Customer {
    INSTANCE;

    public String displayName() { return "guest"; }
    public int discountPercent() { return 0; }            // neutral value
    public void notify(String message) { }                // does nothing, on purpose
    @Override public boolean isGuest() { return true; }   // for the rare caller that must know
}
