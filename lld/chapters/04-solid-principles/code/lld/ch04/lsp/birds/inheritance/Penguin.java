package lld.ch04.lsp.birds.inheritance;

/** VIOLATION ("refused bequest"): inherits fly() and has to refuse it. */
public class Penguin extends Bird {
    @Override
    public void fly() {
        throw new UnsupportedOperationException("Penguins can't fly");
    }
}
