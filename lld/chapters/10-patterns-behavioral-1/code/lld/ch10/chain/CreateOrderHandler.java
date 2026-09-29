package lld.ch10.chain;

/** Terminal handler: does the real work and never passes on. */
final class CreateOrderHandler extends Handler {
    @Override
    public boolean handle(Request r) {
        System.out.println("  [order] created order for " + r.user() + ": " + r.payload());
        return true;
    }
}
