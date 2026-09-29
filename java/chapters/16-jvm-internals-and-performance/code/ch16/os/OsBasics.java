package ch16.os;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.*;
import java.nio.channels.spi.SelectorProvider;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * 16.13 Operating-system ideas seen from Java: processes vs threads, file descriptors,
 * virtual memory (a 1 GiB mapping that costs almost no RAM) and non-blocking I/O with one selector thread.
 * Usage: java -cp out ch16.os.OsBasics      (loopback only; creates and deletes one temp file)
 */
public class OsBasics {

    static int sharedCounter = 0;   // lives in THIS process's heap

    public static void main(String[] args) throws Exception {
        // 1. Process: its own id, parent, address space.  Threads: share that address space.
        ProcessHandle me = ProcessHandle.current();
        System.out.printf("process  pid=%d parent=%d cpus=%d%n", me.pid(),
                me.parent().map(ProcessHandle::pid).orElse(-1L), Runtime.getRuntime().availableProcessors());
        Thread t = Thread.ofPlatform().start(() -> sharedCounter += 42);   // another thread, same memory
        t.join();
        System.out.println("thread   wrote sharedCounter -> main thread reads " + sharedCounter);

        Process child = new ProcessBuilder("sh", "-c", "echo \"child pid=$$ parent=$PPID\"; exit 3")
                .redirectErrorStream(true).start();                          // fork + exec a new process
        String out = new String(child.getInputStream().readAllBytes()).trim();
        System.out.println("child    says: " + out + " | exit code " + child.waitFor()
                + " | it could not see sharedCounter (separate address space)");

        // 2. File descriptors: every open file/socket is a small integer in a per-process table.
        long before = openFds();
        List<FileChannel> open = new ArrayList<>();
        Path tmp = Files.createTempFile("os-basics", ".bin");
        for (int i = 0; i < 100; i++) open.add(FileChannel.open(tmp));
        long during = openFds();
        for (FileChannel c : open) c.close();
        System.out.printf("fds      before=%d  with 100 files open=%d  after close=%d%n", before, during, openFds());

        // 3. Virtual memory: map 1 GiB of a sparse file; physical pages are allocated only when touched.
        String pageSize = new String(new ProcessBuilder("getconf", "PAGESIZE").start().getInputStream().readAllBytes()).trim();
        int page = Integer.parseInt(pageSize);
        try (RandomAccessFile f = new RandomAccessFile(tmp.toFile(), "rw"); FileChannel ch = f.getChannel()) {
            f.setLength(1L << 30);                                            // 1 GiB, but sparse on disk
            MappedByteBuffer map = ch.map(FileChannel.MapMode.READ_WRITE, 0, 1L << 30);
            for (int p = 0; p < 1000; p++) map.put(p * page, (byte) 1);      // touch 1000 pages -> page faults
            System.out.printf("vm       page size=%d B, mapped 1024 MiB, touched 1000 pages -> only those %.1f MiB need physical RAM%n",
                    page, 1000.0 * page / (1 << 20));
        }
        Files.delete(tmp);

        // 4. Non-blocking I/O: ONE thread serves 200 connections through the OS readiness API (epoll/kqueue).
        System.out.println("nio      selector implementation: " + SelectorProvider.provider().getClass().getSimpleName());
        System.out.println("nio      one selector thread echoed to " + echoWithOneThread(200) + " of 200 clients");
    }

    static long openFds() throws IOException {
        Path dir = Files.isDirectory(Path.of("/proc/self/fd")) ? Path.of("/proc/self/fd") : Path.of("/dev/fd");
        try (var s = Files.list(dir)) { return s.count(); }
    }

    static int echoWithOneThread(int clients) throws Exception {
        try (Selector sel = Selector.open(); ServerSocketChannel server = ServerSocketChannel.open()) {
            server.bind(new InetSocketAddress("127.0.0.1", 0)).configureBlocking(false);
            server.register(sel, SelectionKey.OP_ACCEPT);
            AtomicInteger served = new AtomicInteger();
            Thread loop = Thread.ofPlatform().name("event-loop").start(() -> {
                try {
                    while (served.get() < clients) {
                        sel.select(100);                                     // one syscall waits on ALL sockets
                        for (var it = sel.selectedKeys().iterator(); it.hasNext(); ) {
                            SelectionKey k = it.next(); it.remove();
                            if (k.isAcceptable()) {
                                SocketChannel c = server.accept();
                                if (c != null) c.configureBlocking(false).register(sel, SelectionKey.OP_READ);
                            } else if (k.isReadable()) {
                                SocketChannel c = (SocketChannel) k.channel();
                                ByteBuffer b = ByteBuffer.allocate(16);
                                if (c.read(b) > 0) { b.flip(); c.write(b); served.incrementAndGet(); }
                                c.close();
                            }
                        }
                    }
                } catch (IOException e) { throw new RuntimeException(e); }
            });
            int port = ((InetSocketAddress) server.getLocalAddress()).getPort();
            List<SocketChannel> socks = new ArrayList<>();
            for (int i = 0; i < clients; i++) socks.add(SocketChannel.open(new InetSocketAddress("127.0.0.1", port)));
            int ok = 0;
            for (SocketChannel s : socks) {                                  // clients are blocking, for simplicity
                s.write(ByteBuffer.wrap("ping".getBytes()));
                ByteBuffer r = ByteBuffer.allocate(16);
                if (s.read(r) == 4) ok++;
                s.close();
            }
            loop.join(10_000);
            if (ok != clients) throw new AssertionError("echoed " + ok);
            return ok;
        }
    }
}
