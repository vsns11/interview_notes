package lld.ch02.access.core;

/** Shows all four access levels on one class. */
public class Wallet {
    public final String owner;                 // public: visible everywhere
    protected long bonusCents;                 // protected: same package + subclasses anywhere
    long auditCount;                           // package-private (no keyword): same package only
    private long balanceCents;                 // private: this class only

    public Wallet(String owner) { this.owner = owner; }

    public void add(long cents) {
        if (cents <= 0) throw new IllegalArgumentException("positive only");
        balanceCents += cents;
        AuditLog.record(this, "add " + cents);   // package-private helper class, invisible outside core
    }

    public long balanceCents() { return balanceCents; }

    protected String describe() { return owner + " has " + balanceCents + "c (+" + bonusCents + "c bonus)"; }
}
