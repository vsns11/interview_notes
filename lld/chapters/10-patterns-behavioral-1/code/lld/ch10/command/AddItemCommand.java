package lld.ch10.command;

/** Adds a line; undo removes exactly that line. */
final class AddItemCommand implements UndoableCommand {
    private final Cart cart;      // the command carries its receiver...
    private final String sku;     // ...and all the arguments it needs to run later
    private final int qty;

    AddItemCommand(Cart cart, String sku, int qty) {
        this.cart = cart;
        this.sku = sku;
        this.qty = qty;
    }

    @Override public void execute() { cart.add(sku, qty); }
    @Override public void undo()    { cart.remove(sku, qty); }
    @Override public String name()  { return "AddItem(" + sku + ", " + qty + ")"; }
}
