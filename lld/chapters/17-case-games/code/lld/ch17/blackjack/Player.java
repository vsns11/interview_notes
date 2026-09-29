package lld.ch17.blackjack;

/** A seated player: a name, a chip balance (whole chips) and a decision strategy. */
public final class Player {
    private final String name;
    private final PlayerStrategy strategy;
    private int balance;

    public Player(String name, int balance, PlayerStrategy strategy) {
        this.name = name;
        this.balance = balance;
        this.strategy = strategy;
    }

    void debit(int amount) {
        if (amount > balance) throw new IllegalStateException(name + " cannot cover " + amount);
        balance -= amount;
    }

    void credit(int amount) { balance += amount; }

    public boolean canAfford(int amount) { return balance >= amount; }
    public String name() { return name; }
    public int balance() { return balance; }
    public PlayerStrategy strategy() { return strategy; }
}
