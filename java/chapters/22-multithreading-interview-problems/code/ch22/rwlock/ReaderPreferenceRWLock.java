package ch22.rwlock;

/**
 * Reader preference: a reader may enter whenever no writer is INSIDE.
 * Maximum read throughput, but a steady stream of overlapping readers keeps
 * readers > 0 forever, so a waiting writer can starve.
 */
public final class ReaderPreferenceRWLock implements SimpleRWLock {
    private int readers;           // readers currently inside
    private boolean writing;       // a writer is inside

    @Override
    public synchronized void lockRead() throws InterruptedException {
        while (writing) wait();
        readers++;
    }

    @Override
    public synchronized void unlockRead() {
        if (readers <= 0) throw new IllegalMonitorStateException("no read lock held");
        readers--;
        if (readers == 0) notifyAll();         // the last reader out lets a writer in
    }

    @Override
    public synchronized void lockWrite() throws InterruptedException {
        while (writing || readers > 0) wait();
        writing = true;
    }

    @Override
    public synchronized void unlockWrite() {
        if (!writing) throw new IllegalMonitorStateException("no write lock held");
        writing = false;
        notifyAll();                           // wake all readers AND writers; they re-check
    }

    @Override
    public String name() {
        return "reader-preference";
    }
}
