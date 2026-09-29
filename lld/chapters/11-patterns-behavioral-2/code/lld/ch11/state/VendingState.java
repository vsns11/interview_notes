package lld.ch11.state;

/** Vending machine states. Default methods = "refuse", so each state overrides only what it allows. */
interface VendingState {
    String name();

    default void insertCoin(VendingMachine m, int cents) {
        System.out.println("  [" + name() + "] coin rejected, returned " + cents);
    }

    default void select(VendingMachine m, String code) {
        System.out.println("  [" + name() + "] selection ignored");
    }

    default void cancel(VendingMachine m) {
        System.out.println("  [" + name() + "] nothing to cancel");
    }
}
