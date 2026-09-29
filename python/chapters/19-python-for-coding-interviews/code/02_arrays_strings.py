"""19.2 Arrays and strings: two pointers, sliding window, prefix sums."""
from collections import defaultdict
from itertools import accumulate


# ---------- two pointers ----------
def two_sum_sorted(nums: list[int], target: int) -> tuple[int, int] | None:
    """Indices (i, j), i < j, with nums[i] + nums[j] == target. nums is sorted."""
    lo, hi = 0, len(nums) - 1
    while lo < hi:
        s = nums[lo] + nums[hi]
        if s == target:
            return lo, hi
        if s < target:
            lo += 1          # need a bigger sum -> move the small end up
        else:
            hi -= 1          # need a smaller sum -> move the big end down
    return None


def is_palindrome(s: str) -> bool:
    """Ignore case and non-alphanumerics (classic 'valid palindrome')."""
    i, j = 0, len(s) - 1
    while i < j:
        if not s[i].isalnum():
            i += 1
        elif not s[j].isalnum():
            j -= 1
        else:
            if s[i].lower() != s[j].lower():
                return False
            i, j = i + 1, j - 1
    return True


def remove_duplicates_sorted(nums: list[int]) -> int:
    """In place: keep the first copy of each value, return the new length."""
    if not nums:
        return 0
    write = 1                           # slow pointer: next slot to fill
    for read in range(1, len(nums)):    # fast pointer: scans everything
        if nums[read] != nums[write - 1]:
            nums[write] = nums[read]
            write += 1
    return write


# ---------- sliding window ----------
def max_sum_window(nums: list[int], k: int) -> int:
    """Fixed-size window: largest sum of k consecutive numbers."""
    window = sum(nums[:k])
    best = window
    for i in range(k, len(nums)):
        window += nums[i] - nums[i - k]   # add the new right, drop the old left
        best = max(best, window)
    return best


def longest_unique_substring(s: str) -> int:
    """Variable-size window: longest substring without repeating characters."""
    last_seen: dict[str, int] = {}
    left = best = 0
    for right, ch in enumerate(s):
        if ch in last_seen and last_seen[ch] >= left:
            left = last_seen[ch] + 1      # shrink: jump past the earlier copy
        last_seen[ch] = right
        best = max(best, right - left + 1)
    return best


def min_window_at_least(nums: list[int], target: int) -> int:
    """Shortest subarray (positive numbers) with sum >= target, 0 if none."""
    left = total = 0
    best = float("inf")
    for right, x in enumerate(nums):
        total += x
        while total >= target:            # shrink while still valid
            best = min(best, right - left + 1)
            total -= nums[left]
            left += 1
    return 0 if best == float("inf") else int(best)


# ---------- prefix sums ----------
class RangeSum:
    """O(n) build, O(1) query of sum(nums[i:j])."""

    def __init__(self, nums: list[int]):
        self.prefix = [0, *accumulate(nums)]   # prefix[k] = sum(nums[:k])

    def query(self, i: int, j: int) -> int:     # half-open [i, j)
        return self.prefix[j] - self.prefix[i]


def count_subarrays_sum_k(nums: list[int], k: int) -> int:
    """Number of contiguous subarrays summing to k (works with negatives)."""
    seen = defaultdict(int)
    seen[0] = 1           # the empty prefix
    running = count = 0
    for x in nums:
        running += x
        count += seen[running - k]   # earlier prefixes that leave exactly k
        seen[running] += 1
    return count


if __name__ == "__main__":
    assert two_sum_sorted([1, 3, 4, 6, 9], 10) == (0, 4)
    assert two_sum_sorted([1, 2], 7) is None
    assert is_palindrome("A man, a plan, a canal: Panama")
    assert not is_palindrome("race a car")
    arr = [1, 1, 2, 3, 3, 3, 4]
    n = remove_duplicates_sorted(arr)
    assert arr[:n] == [1, 2, 3, 4]

    assert max_sum_window([2, 1, 5, 1, 3, 2], 3) == 9
    assert longest_unique_substring("abcabcbb") == 3
    assert longest_unique_substring("pwwkew") == 3
    assert longest_unique_substring("") == 0
    assert min_window_at_least([2, 3, 1, 2, 4, 3], 7) == 2
    assert min_window_at_least([1, 1], 5) == 0

    rs = RangeSum([3, -1, 4, 1, 5])
    print("prefix:", rs.prefix)
    assert rs.query(1, 4) == 4          # -1 + 4 + 1
    assert count_subarrays_sum_k([1, 1, 1], 2) == 2
    assert count_subarrays_sum_k([3, 4, 7, -2, 2, 1, 4, 2], 7) == 6

    print("two_sum_sorted([1,3,4,6,9], 10) ->", two_sum_sorted([1, 3, 4, 6, 9], 10))
    print("longest_unique_substring('pwwkew') ->", longest_unique_substring("pwwkew"))
    print("min_window_at_least([2,3,1,2,4,3], 7) ->", min_window_at_least([2, 3, 1, 2, 4, 3], 7))
    print("count_subarrays_sum_k([3,4,7,-2,2,1,4,2], 7) ->",
          count_subarrays_sum_k([3, 4, 7, -2, 2, 1, 4, 2], 7))
    print("all asserts passed")
