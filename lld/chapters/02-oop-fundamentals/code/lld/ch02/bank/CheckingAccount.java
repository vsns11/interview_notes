package lld.ch02.bank;

/** A checking account charges a monthly fee unless the balance is above a threshold. */
public class CheckingAccount extends Account {
    private final long feeCents;
    private final long waiveAboveCents;

    public CheckingAccount(String id, String owner, long openingCents, long feeCents, long waiveAboveCents) {
        super(id, owner, openingCents);
        this.feeCents = feeCents;
        this.waiveAboveCents = waiveAboveCents;
    }

    @Override
    public String monthlyUpdate() {
        if (balanceCents() >= waiveAboveCents) return "fee waived";
        return withdraw(feeCents) ? "fee " + Money.format(feeCents) : "fee could not be taken";
    }
}
