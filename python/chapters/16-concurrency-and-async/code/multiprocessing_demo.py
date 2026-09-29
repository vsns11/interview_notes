"""multiprocessing: Process, Pool, and why globals are not shared between processes."""
import multiprocessing as mp
import os

counter = 0                               # each process gets its OWN copy


def bump_global(n: int, parent_pid: int) -> None:
    global counter
    for _ in range(n):
        counter += 1
    print(f"  child: different pid from parent={os.getpid() != parent_pid}, sees counter={counter}")


def bump_shared(shared, n: int) -> None:
    for _ in range(n):
        with shared.get_lock():           # Value comes with its own lock
            shared.value += 1


def square(x: int) -> int:
    return x * x


if __name__ == "__main__":                # REQUIRED with spawn/forkserver start methods
    print(f"start method: {mp.get_start_method()}, CPUs: {os.cpu_count()}")

    # 1. A single Process
    p = mp.Process(target=bump_global, args=(1000, os.getpid()))
    p.start()
    p.join()
    print(f"parent counter after child ran: {counter}  (not shared!)  exitcode={p.exitcode}")

    # 2. Shared memory with Value
    shared = mp.Value("i", 0)             # 'i' = C int in shared memory
    procs = [mp.Process(target=bump_shared, args=(shared, 1000)) for _ in range(4)]
    for pr in procs:
        pr.start()
    for pr in procs:
        pr.join()
    print(f"shared Value after 4 x 1000 increments: {shared.value}")

    # 3. Pool: map a function over data in parallel
    with mp.Pool(processes=4) as pool:
        print(f"pool.map squares: {pool.map(square, range(10))}")
        print(f"pool.map chunksize=3: {pool.map(square, range(10), chunksize=3)[:5]} ...")

    # 4. Lambdas cannot be pickled -> cannot be sent to a worker process
    try:
        with mp.Pool(2) as pool:
            pool.map(lambda x: x + 1, [1, 2])
    except Exception as e:
        print(f"lambda in Pool -> {type(e).__name__}")
