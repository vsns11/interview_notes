"""19.11 Backtracking: choose -> explore -> un-choose."""
from itertools import combinations, permutations


def subsets(nums: list[int]) -> list[list[int]]:
    out, path = [], []

    def go(start: int) -> None:
        out.append(path[:])              # copy! path keeps changing
        for i in range(start, len(nums)):
            path.append(nums[i])         # choose
            go(i + 1)                    # explore
            path.pop()                   # un-choose
    go(0)
    return out


def permute(nums: list[int]) -> list[list[int]]:
    out, path, used = [], [], [False] * len(nums)

    def go() -> None:
        if len(path) == len(nums):
            out.append(path[:])
            return
        for i, x in enumerate(nums):
            if not used[i]:
                used[i] = True
                path.append(x)
                go()
                path.pop()
                used[i] = False
    go()
    return out


def combination_sum(cands: list[int], target: int) -> list[list[int]]:
    """Each candidate may be reused. Sorting enables early pruning."""
    cands = sorted(cands)
    out, path = [], []

    def go(start: int, remaining: int) -> None:
        if remaining == 0:
            out.append(path[:])
            return
        for i in range(start, len(cands)):
            if cands[i] > remaining:
                break                    # prune: later ones are even bigger
            path.append(cands[i])
            go(i, remaining - cands[i])  # i, not i+1: reuse allowed
            path.pop()
    go(0, target)
    return out


def n_queens(n: int) -> int:
    cols, diag, anti = set(), set(), set()

    def go(r: int) -> int:
        if r == n:
            return 1
        count = 0
        for c in range(n):
            if c in cols or r - c in diag or r + c in anti:
                continue
            cols.add(c); diag.add(r - c); anti.add(r + c)
            count += go(r + 1)
            cols.remove(c); diag.remove(r - c); anti.remove(r + c)
        return count
    return go(0)


def word_search(board: list[list[str]], word: str) -> bool:
    rows, cols = len(board), len(board[0])

    def go(r, c, k) -> bool:
        if k == len(word):
            return True
        if not (0 <= r < rows and 0 <= c < cols) or board[r][c] != word[k]:
            return False
        board[r][c] = "#"                         # mark visited in place
        found = any(go(r + dr, c + dc, k + 1) for dr, dc in ((1, 0), (-1, 0), (0, 1), (0, -1)))
        board[r][c] = word[k]                     # restore
        return found

    return any(go(r, c, 0) for r in range(rows) for c in range(cols))


if __name__ == "__main__":
    s = subsets([1, 2, 3])
    print("subsets:", s)
    assert len(s) == 8
    assert sorted(map(tuple, s)) == sorted(t for k in range(4) for t in combinations([1, 2, 3], k))
    p = permute([1, 2, 3])
    assert p == [list(t) for t in permutations([1, 2, 3])]
    print("combination_sum([2,3,6,7], 7):", combination_sum([2, 3, 6, 7], 7))
    assert combination_sum([2, 3, 6, 7], 7) == [[2, 2, 3], [7]]
    assert [n_queens(n) for n in range(1, 9)] == [1, 0, 0, 2, 10, 4, 40, 92]
    board = [list("ABCE"), list("SFCS"), list("ADEE")]
    assert word_search(board, "ABCCED") and not word_search(board, "ABCB")
    print("8-queens solutions:", n_queens(8))
    print("all asserts passed")
