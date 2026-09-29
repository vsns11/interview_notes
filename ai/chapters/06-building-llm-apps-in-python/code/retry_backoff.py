"""Retries with exponential backoff + jitter, retry-after, timeouts, and a
client-side token bucket - against a flaky fake API. A simulated clock keeps
the demo instant and deterministic (no real sleeping)."""
import random


class RateLimitError(Exception):
    def __init__(self, retry_after):
        super().__init__("429 rate limited")
        self.retry_after = retry_after


class ServerError(Exception):       # 500/529 overloaded - retryable
    pass


class BadRequestError(Exception):   # 400 - your bug, never retry
    pass


class Clock:
    def __init__(self):
        self.t = 0.0

    def sleep(self, s):
        self.t += s


class FlakyAPI:
    """Fails the first few calls in different ways, then succeeds."""

    def __init__(self, script, clock):
        self.script, self.clock = list(script), clock

    def call(self, prompt, timeout):
        outcome = self.script.pop(0) if self.script else "ok"
        if outcome == "429":
            raise RateLimitError(retry_after=2.0)
        if outcome == "500":
            raise ServerError("500 internal error")
        if outcome == "slow":
            self.clock.sleep(timeout)           # we waited the whole timeout
            raise TimeoutError(f"no response within {timeout}s")
        if outcome == "400":
            raise BadRequestError("400 invalid request: max_tokens too large")
        return f"answer to {prompt!r}"


def call_with_retries(api, prompt, clock, max_retries=4, base=0.5, cap=8.0,
                      timeout=10.0, rng=random.Random(0)):
    for attempt in range(max_retries + 1):
        try:
            result = api.call(prompt, timeout)
            print(f"  t={clock.t:5.2f}s attempt {attempt + 1}: success")
            return result
        except BadRequestError as e:
            print(f"  t={clock.t:5.2f}s attempt {attempt + 1}: {e} -> not retryable")
            raise
        except (RateLimitError, ServerError, TimeoutError) as e:
            if attempt == max_retries:
                print(f"  giving up after {attempt + 1} attempts")
                raise
            if isinstance(e, RateLimitError):          # the server told us how long
                wait = e.retry_after
            else:                                       # full jitter: U(0, min(cap, base*2^n))
                wait = rng.uniform(0, min(cap, base * 2 ** attempt))
            print(f"  t={clock.t:5.2f}s attempt {attempt + 1}: {e} -> wait {wait:.2f}s")
            clock.sleep(wait)


class TokenBucket:
    """Allow `rate` requests per second on average, bursts up to `capacity`."""

    def __init__(self, rate, capacity, clock):
        self.rate, self.capacity, self.clock = rate, capacity, clock
        self.tokens, self.last = capacity, clock.t

    def acquire(self):
        while True:
            now = self.clock.t
            self.tokens = min(self.capacity, self.tokens + (now - self.last) * self.rate)
            self.last = now
            if self.tokens >= 1:
                self.tokens -= 1
                return
            self.clock.sleep((1 - self.tokens) / self.rate)   # wait for the next token


if __name__ == "__main__":
    clock = Clock()
    print("Flaky API: 429, 500, timeout, then OK")
    api = FlakyAPI(["429", "500", "slow", "ok"], clock)
    print(" ->", call_with_retries(api, "hello", clock))

    print("\nA 400 error is raised immediately:")
    try:
        call_with_retries(FlakyAPI(["400"], clock), "hello", clock)
    except BadRequestError:
        print(" -> caller fixes the request instead of retrying")

    print("\nToken bucket: 2 requests/second, burst of 3, 8 requests arrive at once")
    clock = Clock()
    bucket = TokenBucket(rate=2, capacity=3, clock=clock)
    times = []
    for _ in range(8):
        bucket.acquire()
        times.append(round(clock.t, 2))
    print("  send times (s):", times)
