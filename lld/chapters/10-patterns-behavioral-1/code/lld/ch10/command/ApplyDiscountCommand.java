package lld.ch10.command;

/** Sets a discount; captures the previous value during execute so undo RESTORES, never guesses. */
final class ApplyDiscountCommand implements UndoableCommand {
    private final Cart cart;
    private final int percent;
    private int previousPercent;          // filled in by execute()

    ApplyDiscountCommand(Cart cart, int percent) {
        this.cart = cart;
        this.percent = percent;
    }

    @Override
    public void execute() {
        previousPercent = cart.discountPercent();   // capture BEFORE changing
        cart.setDiscount(percent);
    }

    @Override public void undo()   { cart.setDiscount(previousPercent); }
    @Override public String name() { return "ApplyDiscount(" + percent + "%)"; }
}
