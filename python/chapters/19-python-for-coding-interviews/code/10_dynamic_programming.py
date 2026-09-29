"""19.10 Dynamic programming: top-down with @cache, bottom-up tabulation."""
import bisect
from functools import cache


# ---- 1-D ----
@cache
def climb(n: int) -> int:
    """Ways to climb n steps taking 1 or 2 at a time."""
    return 1 if n <= 1 else climb(n - 1) + climb(n - 2)


def climb_tab(n: int) -> int:
    a, b = 1, 1                  # ways(0), ways(1) - O(1) space
    for _ in range(n - 1):
        a, b = b, a + b
    return b


def house_robber(nums: list[int]) -> int:
    take = skip = 0
    for x in nums:
        take, skip = skip + x, max(take, skip)
    return max(take, skip)


def coin_change(coins: list[int], amount: int) -> int:
    INF = amount + 1
    dp = [0] + [INF] * amount     # dp[a] = fewest coins to make a
    for a in range(1, amount + 1):
        for c in coins:
            if c <= a:
                dp[a] = min(dp[a], dp[a - c] + 1)
    return dp[amount] if dp[amount] != INF else -1


def lis_length(nums: list[int]) -> int:
    """O(n log n): tails[k] = smallest tail of an increasing subsequence of length k+1."""
    tails: list[int] = []
    for x in nums:
        i = bisect.bisect_left(tails, x)
        if i == len(tails):
            tails.append(x)
        else:
            tails[i] = x
    return len(tails)


# ---- 2-D ----
def lcs(a: str, b: str) -> int:
    m, n = len(a), len(b)
    dp = [[0] * (n + 1) for _ in range(m + 1)]    # NOT [[0]*(n+1)]*(m+1)
    for i in range(1, m + 1):
        for j in range(1, n + 1):
            if a[i - 1] == b[j - 1]:
                dp[i][j] = dp[i - 1][j - 1] + 1
            else:
                dp[i][j] = max(dp[i - 1][j], dp[i][j - 1])
    return dp[m][n]


def edit_distance(a: str, b: str) -> int:
    @cache
    def go(i: int, j: int) -> int:
        if i == len(a):
            return len(b) - j
        if j == len(b):
            return len(a) - i
        if a[i] == b[j]:
            return go(i + 1, j + 1)
        return 1 + min(go(i + 1, j),       # delete
                       go(i, j + 1),       # insert
                       go(i + 1, j + 1))   # replace
    return go(0, 0)


def knapsack_01(weights: list[int], values: list[int], cap: int) -> int:
    dp = [0] * (cap + 1)
    for w, v in zip(weights, values):
        for c in range(cap, w - 1, -1):    # backwards: each item used at most once
            dp[c] = max(dp[c], dp[c - w] + v)
    return dp[cap]


def unique_paths(m: int, n: int) -> int:
    row = [1] * n
    for _ in range(1, m):
        for j in range(1, n):
            row[j] += row[j - 1]
    return row[-1]


if __name__ == "__main__":
    assert climb(10) == climb_tab(10) == 89
    print("climb.cache_info():", climb.cache_info())
    assert house_robber([2, 7, 9, 3, 1]) == 12
    assert coin_change([1, 2, 5], 11) == 3 and coin_change([2], 3) == -1
    assert lis_length([10, 9, 2, 5, 3, 7, 101, 18]) == 4
    assert lcs("abcde", "ace") == 3
    assert edit_distance("horse", "ros") == 3
    assert edit_distance("intention", "execution") == 5
    assert knapsack_01([1, 3, 4, 5], [1, 4, 5, 7], 7) == 9
    assert unique_paths(3, 7) == 28
    print("coin_change([1,2,5], 11) =", coin_change([1, 2, 5], 11))
    print("edit_distance('horse','ros') =", edit_distance("horse", "ros"))
    print("knapsack =", knapsack_01([1, 3, 4, 5], [1, 4, 5, 7], 7))
    print("all asserts passed")
