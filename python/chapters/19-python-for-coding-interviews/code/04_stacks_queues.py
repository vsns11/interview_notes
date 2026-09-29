"""19.4 Stacks (list) and queues (deque), incl. monotonic stack / deque."""
from collections import deque


def valid_parentheses(s: str) -> bool:
    pairs = {")": "(", "]": "[", "}": "{"}
    stack: list[str] = []
    for ch in s:
        if ch in "([{":
            stack.append(ch)                  # push
        elif not stack or stack.pop() != pairs[ch]:   # pop must match
            return False
    return not stack                          # leftovers = unclosed


def next_greater(nums: list[int]) -> list[int]:
    """For each element, the next element to the right that is larger (-1 if none).
    Monotonic stack of indices whose values are decreasing."""
    result = [-1] * len(nums)
    stack: list[int] = []
    for i, x in enumerate(nums):
        while stack and nums[stack[-1]] < x:
            result[stack.pop()] = x
        stack.append(i)
    return result


def daily_temperatures(temps: list[int]) -> list[int]:
    """Days to wait for a warmer day (0 if never)."""
    wait = [0] * len(temps)
    stack: list[int] = []
    for i, t in enumerate(temps):
        while stack and temps[stack[-1]] < t:
            j = stack.pop()
            wait[j] = i - j
        stack.append(i)
    return wait


def sliding_window_max(nums: list[int], k: int) -> list[int]:
    """O(n) with a deque of indices whose values are decreasing."""
    dq: deque[int] = deque()
    out = []
    for i, x in enumerate(nums):
        if dq and dq[0] <= i - k:
            dq.popleft()                   # front fell out of the window
        while dq and nums[dq[-1]] <= x:
            dq.pop()                       # smaller values can never be the max
        dq.append(i)
        if i >= k - 1:
            out.append(nums[dq[0]])
    return out


class MinStack:
    """Stack with O(1) get_min: store (value, min so far) pairs."""

    def __init__(self):
        self._items: list[tuple[int, int]] = []

    def push(self, x: int) -> None:
        current_min = min(x, self._items[-1][1]) if self._items else x
        self._items.append((x, current_min))

    def pop(self) -> int:
        return self._items.pop()[0]

    def get_min(self) -> int:
        return self._items[-1][1]


def eval_rpn(tokens: list[str]) -> int:
    stack: list[int] = []
    ops = {
        "+": lambda a, b: a + b,
        "-": lambda a, b: a - b,
        "*": lambda a, b: a * b,
        "/": lambda a, b: int(a / b),   # truncate toward zero, NOT a // b
    }
    for t in tokens:
        if t in ops:
            b, a = stack.pop(), stack.pop()
            stack.append(ops[t](a, b))
        else:
            stack.append(int(t))
    return stack[0]


if __name__ == "__main__":
    assert valid_parentheses("([]{})") and not valid_parentheses("(]") and not valid_parentheses("((")
    assert next_greater([2, 1, 2, 4, 3]) == [4, 2, 4, -1, -1]
    assert daily_temperatures([73, 74, 75, 71, 69, 72, 76, 73]) == [1, 1, 4, 2, 1, 1, 0, 0]
    assert sliding_window_max([1, 3, -1, -3, 5, 3, 6, 7], 3) == [3, 3, 5, 5, 6, 7]
    ms = MinStack()
    for v in [5, 3, 7, 2]:
        ms.push(v)
    assert ms.get_min() == 2
    ms.pop()
    assert ms.get_min() == 3
    assert eval_rpn(["10", "6", "9", "3", "+", "-11", "*", "/", "*", "17", "+", "5", "+"]) == 22
    assert eval_rpn(["7", "-2", "/"]) == -3   # -7//2 would give -4

    q = deque([1, 2, 3])
    q.append(4)          # enqueue at the right
    q.appendleft(0)      # push at the left
    print("deque:", list(q))
    front, back = q.popleft(), q.pop()   # O(1) at both ends
    print("popleft ->", front, "| pop ->", back, "| now", q)
    q.rotate(1)
    print("after rotate(1):", q)
    last3 = deque(maxlen=3)
    for x in range(6):
        last3.append(x)
    print("deque(maxlen=3) after 0..5:", last3)
    print("sliding_window_max:", sliding_window_max([1, 3, -1, -3, 5, 3, 6, 7], 3))
    print("all asserts passed")
