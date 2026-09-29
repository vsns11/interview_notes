package lld.ch04.lsp.birds.composition;

/** The behaviour that varies, pulled out into its own small type. */
@FunctionalInterface
public interface MovementStyle {
    void move(String birdName);
}
