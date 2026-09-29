package lld.ch09.proxy;

/**
 * SMART REFERENCE PROXY: reference counting. Each client gets its own handle (a proxy).
 * The real device opens on the first handle and closes when the LAST handle closes.
 * Thread-safe: every read or write of 'refs', 'real' and a handle's 'closed' flag
 * happens while holding the SharedDevice's lock, so the count can never go wrong.
 */
final class SharedDevice {
    private int refs;              // guarded by 'this'
    private RealDevice real;       // guarded by 'this'

    synchronized Device acquire() {
        if (refs++ == 0) real = new RealDevice();
        System.out.println("    [ref] handle acquired, count=" + refs);
        return new Handle();
    }

    private final class Handle implements Device {
        private boolean closed;    // guarded by SharedDevice.this

        @Override
        public String query(String command) {
            synchronized (SharedDevice.this) {
                if (closed) throw new IllegalStateException("handle already closed");
                return real.query(command);
            }
        }

        @Override
        public void close() {
            synchronized (SharedDevice.this) {
                if (closed) return;          // closing a handle twice must not double-decrement
                closed = true;
                refs--;
                System.out.println("    [ref] handle released, count=" + refs);
                if (refs == 0) {
                    real.close();
                    real = null;
                }
            }
        }
    }
}
