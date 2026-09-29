"""Threads: create/join, a race condition and its fix, and a queue-based producer-consumer."""
import queue
import threading
import time


# ---------- 1. Creating and joining threads ----------
def worker(name: str, delay: float, results: dict) -> None:
    time.sleep(delay)                     # stands in for waiting on I/O
    results[name] = threading.current_thread().name


def demo_create_join() -> None:
    results: dict[str, str] = {}
    threads = [threading.Thread(target=worker, args=(f"job{i}", 0.2, results), name=f"T{i}")
               for i in range(3)]
    t0 = time.perf_counter()
    for t in threads:
        t.start()                          # begins running worker() concurrently
    for t in threads:
        t.join()                           # wait for each one to finish
    print(f"3 x 0.2 s sleeps in threads took {time.perf_counter() - t0:.2f} s -> {results}")


# ---------- 2. Race condition ----------
counter = 0


def unsafe_increment(n: int) -> None:
    global counter
    for _ in range(n):
        current = counter                  # read
        time.sleep(0)                      # yield: lets another thread run (widens the window)
        counter = current + 1              # write back a stale value


lock = threading.Lock()


def safe_increment(n: int) -> None:
    global counter
    for _ in range(n):
        with lock:                         # only one thread inside at a time
            current = counter
            time.sleep(0)
            counter = current + 1


def run(target, n_threads: int = 4, n: int = 1_000) -> int:
    global counter
    counter = 0
    ts = [threading.Thread(target=target, args=(n,)) for _ in range(n_threads)]
    for t in ts:
        t.start()
    for t in ts:
        t.join()
    return counter


# ---------- 3. Producer-consumer with queue.Queue ----------
SENTINEL = None                            # "no more work" marker


def producer(q: queue.Queue, items: list[int]) -> None:
    for item in items:
        q.put(item)                        # blocks if the queue is full (backpressure)
    q.put(SENTINEL)


def consumer(q: queue.Queue, out: list[int], n_producers: int) -> None:
    finished = 0
    while finished < n_producers:
        item = q.get()                     # blocks until an item is available
        if item is SENTINEL:
            finished += 1
        else:
            out.append(item * item)
        q.task_done()


def demo_producer_consumer() -> None:
    q: queue.Queue = queue.Queue(maxsize=5)   # bounded buffer
    out: list[int] = []
    producers = [threading.Thread(target=producer, args=(q, list(range(i * 5, i * 5 + 5))))
                 for i in range(2)]
    cons = threading.Thread(target=consumer, args=(q, out, len(producers)))
    cons.start()
    for p in producers:
        p.start()
    for p in producers:
        p.join()
    cons.join()
    print(f"consumer squared {len(out)} items, sorted: {sorted(out)}")


if __name__ == "__main__":
    demo_create_join()
    print(f"unsafe counter (expected 4000): {run(unsafe_increment)}")
    print(f"safe counter   (expected 4000): {run(safe_increment)}")
    demo_producer_consumer()
