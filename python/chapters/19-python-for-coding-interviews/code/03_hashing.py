"""19.3 Hashing: dict, set, Counter, defaultdict."""
from collections import Counter, defaultdict


def two_sum(nums: list[int], target: int) -> tuple[int, int] | None:
    """Unsorted input: one pass, remember value -> index."""
    index_of: dict[int, int] = {}
    for i, x in enumerate(nums):
        if target - x in index_of:
            return index_of[target - x], i
        index_of[x] = i
    return None


def is_anagram(a: str, b: str) -> bool:
    return Counter(a) == Counter(b)


def group_anagrams(words: list[str]) -> list[list[str]]:
    groups: defaultdict[tuple[str, ...], list[str]] = defaultdict(list)
    for w in words:
        groups[tuple(sorted(w))].append(w)   # key must be hashable -> tuple
    return list(groups.values())


def first_unique_char(s: str) -> int:
    counts = Counter(s)
    for i, ch in enumerate(s):
        if counts[ch] == 1:
            return i
    return -1


def longest_consecutive(nums: list[int]) -> int:
    """O(n): only start counting at the beginning of a run."""
    values = set(nums)
    best = 0
    for x in values:
        if x - 1 not in values:           # x starts a run
            length = 1
            while x + length in values:
                length += 1
            best = max(best, length)
    return best


def contains_duplicate_within_k(nums: list[int], k: int) -> bool:
    last: dict[int, int] = {}
    for i, x in enumerate(nums):
        if x in last and i - last[x] <= k:
            return True
        last[x] = i
    return False


if __name__ == "__main__":
    assert two_sum([2, 7, 11, 15], 9) == (0, 1)
    assert two_sum([3, 3], 6) == (0, 1)
    assert is_anagram("listen", "silent") and not is_anagram("ab", "abb")
    groups = group_anagrams(["eat", "tea", "tan", "ate", "nat", "bat"])
    assert sorted(map(sorted, groups)) == [["ate", "eat", "tea"], ["bat"], ["nat", "tan"]]
    assert first_unique_char("leetcode") == 0
    assert first_unique_char("aabb") == -1
    assert longest_consecutive([100, 4, 200, 1, 3, 2]) == 4
    assert contains_duplicate_within_k([1, 2, 3, 1], 3)
    assert not contains_duplicate_within_k([1, 2, 3, 1], 2)

    c = Counter("mississippi")
    print("Counter:", c)
    print("most_common(2):", c.most_common(2))
    print("missing key:", c["z"])            # 0, and no KeyError
    print("c - Counter('sip'):", c - Counter("sip"))
    print("group_anagrams:", groups)
    print("all asserts passed")
