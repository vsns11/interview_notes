package lld.ch10.chain;

/** Stage 3: the payload must not be empty. */
final class ValidationHandler extends Handler {
    @Override
    public boolean handle(Request r) {
        if (r.payload() == null || r.payload().isBlank()) {
            System.out.println("  [validate] rejected: empty payload");
            return false;
        }
        System.out.println("  [validate] ok");
        return passOn(r);
    }
}
