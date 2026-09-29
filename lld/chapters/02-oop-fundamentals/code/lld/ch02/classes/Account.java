package lld.ch02.classes;

/** A CLASS is the blueprint. Every Account OBJECT gets its own copy of the instance fields. */
public class Account {
    // static = belongs to the CLASS, shared by all objects
    private static int openedCount = 0;
    public static final long MIN_OPENING_CENTS = 0;

    // instance fields = belong to EACH object
    private final String id;
    private final String owner;
    private long balanceCents;

    /** Canonical constructor: does all validation in one place. */
    public Account(String id, String owner, long openingCents) {
        if (openingCents < MIN_OPENING_CENTS) throw new IllegalArgumentException("negative opening balance");
        this.id = id;                 // 'this.id' is the field, 'id' is the parameter
        this.owner = owner;
        this.balanceCents = openingCents;
        openedCount++;                // shared counter
    }

    /** Overloaded constructor: chains to the canonical one with this(...). Must be the first statement. */
    public Account(String id, String owner) {
        this(id, owner, 0);
    }

    public Account deposit(long cents) {   // returns 'this' so calls can be chained
        if (cents <= 0) throw new IllegalArgumentException("deposit must be positive");
        this.balanceCents += cents;
        return this;
    }

    public long balanceCents() { return balanceCents; }
    public String id()         { return id; }
    public String owner()      { return owner; }

    public static int openedCount() { return openedCount; }  // static method: no 'this' available here

    @Override public String toString() { return id + "(" + owner + ", " + balanceCents + "c)"; }
}
