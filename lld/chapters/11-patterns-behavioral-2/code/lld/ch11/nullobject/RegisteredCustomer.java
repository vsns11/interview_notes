package lld.ch11.nullobject;

public record RegisteredCustomer(String name, int discountPercent, String email) implements Customer {
    public String displayName() { return name; }
    public void notify(String message) { System.out.println("    email to " + email + ": " + message); }
}
