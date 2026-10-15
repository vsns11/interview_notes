package ch22.blockingqueue;

/**
 * Way 1: the object's own monitor (synchronized + wait/notifyAll).
 * One wait set is shared by producers ("not full") and consumers ("not empty"),
 * so we must use notifyAll(): notify() could wake a thread of the wrong kind.
 */
public final class MonitorBuffer<T> implements BoundedBuffer<T> {
    private final Object[] items;   // circular array
    private int head;               // index of the oldest item
    private int count;              // number of items stored

    public MonitorBuffer(int capacity) {
        if (capacity <= 0) throw new IllegalArgumentException("capacity must be > 0");
        items = new Object[capacity];
    }

    @Override
    public synchronized void put(T item) throws InterruptedException {
        while (count == items.length) {     // guard: wait until "not full"
            wait();                          // releases the monitor while waiting
        }
        items[(head + count) % items.length] = item;
        count++;
        notifyAll();                         // a consumer may be waiting for "not empty"
    }

    @Override
    @SuppressWarnings("unchecked")
    public synchronized T take() throws InterruptedException {
        while (count == 0) {                 // guard: wait until "not empty"
            wait();
        }
        T item = (T) items[head];
        items[head] = null;                  // let the GC reclaim the object
        head = (head + 1) % items.length;
        count--;
        notifyAll();                         // a producer may be waiting for "not full"
        return item;
    }

    @Override
    public synchronized int size() {
        return count;
    }

    @Override
    public String name() {
        return "monitor (wait/notifyAll)";
    }
}
