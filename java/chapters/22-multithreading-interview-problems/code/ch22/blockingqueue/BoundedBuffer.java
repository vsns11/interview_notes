package ch22.blockingqueue;

/**
 * A fixed-capacity FIFO buffer shared by producer and consumer threads.
 * put() blocks while the buffer is full; take() blocks while it is empty.
 */
public interface BoundedBuffer<T> {
    void put(T item) throws InterruptedException;

    T take() throws InterruptedException;

    int size();

    String name();
}
