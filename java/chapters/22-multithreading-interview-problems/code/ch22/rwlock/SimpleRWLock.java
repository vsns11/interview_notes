package ch22.rwlock;

/** Minimal read-write lock contract used by the demo. Non-reentrant, no upgrade. */
public interface SimpleRWLock {
    void lockRead() throws InterruptedException;

    void unlockRead();

    void lockWrite() throws InterruptedException;

    void unlockWrite();

    String name();
}
