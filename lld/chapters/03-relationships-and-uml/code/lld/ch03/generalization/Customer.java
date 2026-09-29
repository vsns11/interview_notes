package lld.ch03.generalization;

/** Base class holds only what EVERY customer can do, so no subclass must refuse anything. */
public class Customer {
    protected final String name;
    public Customer(String name) { this.name = name; }
    public String browse(String query) { return name + " browses '" + query + "'"; }
}
