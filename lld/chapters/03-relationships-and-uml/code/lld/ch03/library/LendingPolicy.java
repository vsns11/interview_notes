package lld.ch03.library;

/** Realized by concrete policies. Member depends on this interface, not on a class. */
public interface LendingPolicy {
    int maxBooks(Member m);
}
