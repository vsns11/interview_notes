"""19.14 Greedy: jump game (reach and min jumps), gas station, candy, interval scheduling."""


def can_jump(nums: list[int]) -> bool:
    """nums[i] = max jump length from i. Track the farthest index reachable so far."""
    farthest = 0
    for i, n in enumerate(nums):
        if i > farthest:
            return False               # a gap we can never cross
        farthest = max(farthest, i + n)
    return True


def min_jumps(nums: list[int]) -> int:
    """Implicit BFS by levels: [start, end] is the range reachable with `jumps` jumps."""
    jumps, end, farthest = 0, 0, 0
    for i in range(len(nums) - 1):     # never need to jump from the last index
        farthest = max(farthest, i + nums[i])
        if i == end:                   # used up the current level -> one more jump
            jumps += 1
            end = farthest
    return jumps


def gas_station(gas: list[int], cost: list[int]) -> int:
    """Return the start index for a full loop, or -1.

    If total gas < total cost, impossible. Otherwise, whenever the tank goes
    negative at i, no station in [start, i] can be the answer -> start = i + 1.
    """
    if sum(gas) < sum(cost):
        return -1
    start, tank = 0, 0
    for i, (g, c) in enumerate(zip(gas, cost)):
        tank += g - c
        if tank < 0:
            start, tank = i + 1, 0
    return start


def candy(ratings: list[int]) -> int:
    """Each child >= 1 candy; a higher rating than a neighbour -> more candy. Minimum total.

    Two passes: left-to-right fixes the left-neighbour rule, right-to-left the
    right-neighbour rule; max() keeps both satisfied.
    """
    n = len(ratings)
    c = [1] * n
    for i in range(1, n):
        if ratings[i] > ratings[i - 1]:
            c[i] = c[i - 1] + 1
    for i in range(n - 2, -1, -1):
        if ratings[i] > ratings[i + 1]:
            c[i] = max(c[i], c[i + 1] + 1)
    return sum(c)


def max_non_overlapping(intervals: list[tuple[int, int]]) -> int:
    """Activity selection: sort by END time, take every interval that starts after the last end."""
    count, last_end = 0, float("-inf")
    for start, end in sorted(intervals, key=lambda iv: iv[1]):
        if start >= last_end:
            count += 1
            last_end = end
    return count


if __name__ == "__main__":
    assert can_jump([2, 3, 1, 1, 4]) and not can_jump([3, 2, 1, 0, 4])
    assert can_jump([0])
    assert min_jumps([2, 3, 1, 1, 4]) == 2 and min_jumps([2, 3, 0, 1, 4]) == 2
    assert min_jumps([0]) == 0
    print("can_jump [3,2,1,0,4]:", can_jump([3, 2, 1, 0, 4]), "| min_jumps [2,3,1,1,4]:", min_jumps([2, 3, 1, 1, 4]))

    assert gas_station([1, 2, 3, 4, 5], [3, 4, 5, 1, 2]) == 3
    assert gas_station([2, 3, 4], [3, 4, 3]) == -1
    print("gas station start:", gas_station([1, 2, 3, 4, 5], [3, 4, 5, 1, 2]))

    assert candy([1, 0, 2]) == 5 and candy([1, 2, 2]) == 4
    assert candy([1, 3, 4, 5, 2]) == 11
    print("candy [1,3,4,5,2]:", candy([1, 3, 4, 5, 2]))

    talks = [(1, 4), (3, 5), (0, 6), (5, 7), (3, 9), (5, 9), (6, 10), (8, 11), (8, 12), (2, 14), (12, 16)]
    assert max_non_overlapping(talks) == 4                 # (1,4) (5,7) (8,11) (12,16)
    print("max non-overlapping talks:", max_non_overlapping(talks))
    print("all asserts passed")
