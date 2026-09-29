"""asyncio: coroutines, tasks, gather, TaskGroup, timeouts, semaphores, async iteration, to_thread."""
import asyncio
import time

T0 = time.perf_counter()


def stamp() -> str:
    return f"[{time.perf_counter() - T0:4.2f}s]"


async def fetch(name: str, delay: float) -> str:
    await asyncio.sleep(delay)            # non-blocking wait: the loop runs other tasks meanwhile
    return f"{name}:{delay}"


async def failing(delay: float) -> None:
    await asyncio.sleep(delay)
    raise ValueError("boom")


async def ticker(n: int):                 # async generator
    for i in range(n):
        await asyncio.sleep(0.05)
        yield i


class AsyncResource:                      # async context manager
    async def __aenter__(self):
        print(f"{stamp()} open resource")
        return self

    async def __aexit__(self, *exc):
        print(f"{stamp()} close resource")


def blocking_io() -> str:
    time.sleep(0.3)                       # a sync library call (e.g. requests.get)
    return "sync result"


async def main() -> None:
    global T0

    # 1. A coroutine object does nothing until awaited
    coro = fetch("x", 0)
    print(f"type before await: {type(coro).__name__}")
    print(f"awaited: {await coro}")

    # 2. Sequential awaits vs concurrent gather
    T0 = time.perf_counter()
    await fetch("a", 0.2); await fetch("b", 0.2)
    print(f"{stamp()} two sequential awaits")
    T0 = time.perf_counter()
    results = await asyncio.gather(fetch("a", 0.2), fetch("b", 0.2), fetch("c", 0.1))
    print(f"{stamp()} gather -> {results}  (order = argument order)")

    # 3. create_task schedules immediately
    T0 = time.perf_counter()
    task = asyncio.create_task(fetch("bg", 0.2))
    await asyncio.sleep(0.1)
    print(f"{stamp()} task done yet? {task.done()}")
    value = await task
    print(f"{stamp()} task result {value}")

    # 4. gather(return_exceptions=True) collects errors instead of raising the first
    res = await asyncio.gather(fetch("ok", 0.1), failing(0.05), return_exceptions=True)
    print(f"gather with return_exceptions -> {res}")

    # 5. TaskGroup (3.11+): structured concurrency - one failure cancels the siblings
    T0 = time.perf_counter()
    try:
        async with asyncio.TaskGroup() as tg:
            slow = tg.create_task(fetch("slow", 1.0))
            tg.create_task(failing(0.1))
    except* ValueError as eg:
        print(f"{stamp()} TaskGroup raised {eg.exceptions!r}; slow cancelled={slow.cancelled()}")

    # 6. Timeouts: asyncio.timeout (3.11+) and wait_for
    try:
        async with asyncio.timeout(0.1):
            await fetch("too-slow", 1.0)
    except TimeoutError:
        print("asyncio.timeout -> TimeoutError")
    try:
        await asyncio.wait_for(fetch("too-slow", 1.0), timeout=0.1)
    except TimeoutError:
        print("wait_for -> TimeoutError")

    # 7. Limit concurrency with a Semaphore
    sem = asyncio.Semaphore(2)
    async def limited(i: int) -> int:
        async with sem:                   # at most 2 inside at once
            await asyncio.sleep(0.1)
            return i
    T0 = time.perf_counter()
    out = await asyncio.gather(*(limited(i) for i in range(6)))
    print(f"{stamp()} 6 jobs, 2 at a time -> {out}")

    # 8. async for / async with / async comprehension
    T0 = time.perf_counter()
    async with AsyncResource():
        squares = [i * i async for i in ticker(4)]
    print(f"async comprehension -> {squares}")

    # 9. Blocking call: freezes the loop vs asyncio.to_thread
    T0 = time.perf_counter()
    await asyncio.gather(asyncio.to_thread(blocking_io), asyncio.to_thread(blocking_io))
    print(f"{stamp()} two blocking calls via to_thread ran together")
    T0 = time.perf_counter()
    async def bad():
        return blocking_io()              # time.sleep inside a coroutine blocks EVERYTHING
    await asyncio.gather(bad(), bad())
    print(f"{stamp()} two blocking calls inside coroutines ran one after another")


if __name__ == "__main__":
    asyncio.run(main())                   # creates the event loop, runs main(), closes the loop
