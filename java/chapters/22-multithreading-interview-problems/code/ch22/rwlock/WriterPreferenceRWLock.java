package ch22.rwlock;

/**
 * Writer preference: as soon as a writer is WAITING, new readers are held back.
 * Readers already inside finish, the writer goes next. Writers cannot starve;
 * readers can, if writers keep arriving back to back.
 */
public final class WriterPreferenceRWLock implements SimpleRWLock {
    private int readers;
    private boolean writing;
    private int waitingWriters;    // writers queued at lockWrite()

    @Override
    public synchronized void lockRead() throws InterruptedException {
        while (writing || waitingWriters > 0) wait();   // the one-line difference
        readers++;
    }

    @Override
    public synchronized void unlockRead() {
        if (readers <= 0) throw new IllegalMonitorStateException("no read lock held");
        readers--;
        if (readers == 0) notifyAll();
    }

    @Override
    public synchronized void lockWrite() throws InterruptedException {
        waitingWriters++;
        try {
            while (writing || readers > 0) wait();
        } catch (InterruptedException e) {
            waitingWriters--;          // give up our place, or readers stay blocked forever
            notifyAll();               // readers held back by us must re-check
            throw e;
        }
        waitingWriters--;
        writing = true;
    }

    @Override
    public synchronized void unlockWrite() {
        if (!writing) throw new IllegalMonitorStateException("no write lock held");
        writing = false;
        notifyAll();
    }

    @Override
    public String name() {
        return "writer-preference";
    }
}
