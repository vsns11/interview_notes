"""Pitfalls: deadlock from inconsistent lock order (detected with timeouts), and the fix."""
import threading
import time

lock_a, lock_b = threading.Lock(), threading.Lock()


def transfer(first: threading.Lock, second: threading.Lock, name: str, log: list) -> None:
    with first:
        time.sleep(0.05)                               # make the bad interleaving likely
        if second.acquire(timeout=0.5):                # a real deadlock would wait forever
            second.release()
            log.append(f"{name} ok")
        else:
            log.append(f"{name} gave up: DEADLOCK")


def run(order_1, order_2) -> list:
    log: list[str] = []
    t1 = threading.Thread(target=transfer, args=(*order_1, "T1", log))
    t2 = threading.Thread(target=transfer, args=(*order_2, "T2", log))
    t1.start(); t2.start(); t1.join(); t2.join()
    return log


if __name__ == "__main__":
    print("opposite order :", run((lock_a, lock_b), (lock_b, lock_a)))
    print("same order     :", run((lock_a, lock_b), (lock_a, lock_b)))

    # RLock: the same thread may re-acquire; a plain Lock would block itself
    rlock = threading.RLock()
    with rlock:
        with rlock:
            print("RLock re-entered by the same thread: fine")
    plain = threading.Lock()
    plain.acquire()
    print("plain Lock re-acquire (timeout 0.1):", plain.acquire(timeout=0.1))
    plain.release()
