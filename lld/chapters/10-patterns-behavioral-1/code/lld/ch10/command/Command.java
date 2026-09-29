package lld.ch10.command;

/** A request turned into an object. One abstract method, so a lambda is a Command. */
@FunctionalInterface
public interface Command {
    void execute();

    default String name() { return getClass().getSimpleName(); }
}
