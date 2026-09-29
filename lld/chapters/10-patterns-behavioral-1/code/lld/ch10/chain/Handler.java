package lld.ch10.chain;

/** Classic Chain of Responsibility base: each handler knows only "the next one". */
public abstract class Handler {
    private Handler next;

    /** Links the next handler and returns it, so wiring reads as one chained expression. */
    public Handler setNext(Handler next) {
        this.next = next;
        return next;
    }

    /** Forwards the request. Falling off the end means the chain has no terminal handler: a wiring bug. */
    protected boolean passOn(Request request) {
        if (next == null) {
            throw new IllegalStateException(
                getClass().getSimpleName() + " tried to pass on, but the chain has no terminal handler");
        }
        return next.handle(request);
    }

    /** @return true if the request was fully handled, false if some handler rejected it. */
    public abstract boolean handle(Request request);
}
