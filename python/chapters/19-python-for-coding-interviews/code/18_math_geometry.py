"""19.18 Math & geometry: spiral order, rotate matrix, reverse a 32-bit int, collinear points, Josephus."""
from collections import Counter
from math import gcd

INT_MIN, INT_MAX = -2**31, 2**31 - 1


def spiral_order(matrix: list[list[int]]) -> list[int]:
    """Peel the outer ring with four shrinking boundaries: top, bottom, left, right."""
    out: list[int] = []
    if not matrix:
        return out
    top, bottom, left, right = 0, len(matrix) - 1, 0, len(matrix[0]) - 1
    while top <= bottom and left <= right:
        out += [matrix[top][c] for c in range(left, right + 1)]
        top += 1
        out += [matrix[r][right] for r in range(top, bottom + 1)]
        right -= 1
        if top <= bottom:                                   # a row is left
            out += [matrix[bottom][c] for c in range(right, left - 1, -1)]
            bottom -= 1
        if left <= right:                                   # a column is left
            out += [matrix[r][left] for r in range(bottom, top - 1, -1)]
            left += 1
    return out


def rotate(matrix: list[list[int]]) -> None:
    """Rotate n x n clockwise in place = transpose, then reverse each row."""
    n = len(matrix)
    for i in range(n):
        for j in range(i + 1, n):
            matrix[i][j], matrix[j][i] = matrix[j][i], matrix[i][j]
    for row in matrix:
        row.reverse()


def reverse_int(x: int) -> int:
    """Reverse the digits; return 0 if the result leaves the signed 32-bit range.
    Python ints never overflow, so the check is explicit. (In a 32-bit language you
    must check BEFORE multiplying by 10: res > (INT_MAX - digit) // 10.)"""
    sign = -1 if x < 0 else 1
    x, res = abs(x), 0
    while x:
        x, digit = divmod(x, 10)
        if res > (INT_MAX - digit) // 10:                   # the check a C/Java version needs
            return 0
        res = res * 10 + digit
    res *= sign
    return res if INT_MIN <= res <= INT_MAX else 0


def max_points_on_line(points: list[tuple[int, int]]) -> int:
    """For each anchor, count other points by exact slope (reduced dy/dx, sign-normalised).
    Integers instead of floats avoid 0.1-style rounding bugs. O(n^2)."""
    if len(points) <= 2:
        return len(points)
    best = 0
    for i, (x1, y1) in enumerate(points):
        slopes: Counter = Counter()
        dup = 0
        for x2, y2 in points[i + 1:]:
            dx, dy = x2 - x1, y2 - y1
            if dx == dy == 0:
                dup += 1
                continue
            g = gcd(dx, dy)
            dx, dy = dx // g, dy // g
            if dx < 0 or (dx == 0 and dy < 0):              # one canonical sign per direction
                dx, dy = -dx, -dy
            slopes[(dx, dy)] += 1
        best = max(best, max(slopes.values(), default=0) + dup + 1)
    return best


def josephus(n: int, k: int) -> int:
    """n people in a circle (0-based), every k-th is removed; index of the survivor.
    Recurrence J(1) = 0, J(m) = (J(m-1) + k) % m - O(n) instead of simulating."""
    survivor = 0
    for m in range(2, n + 1):
        survivor = (survivor + k) % m
    return survivor


def josephus_simulated(n: int, k: int) -> int:
    people, i = list(range(n)), 0
    while len(people) > 1:
        i = (i + k - 1) % len(people)
        people.pop(i)                                       # O(n) each -> O(n^2) total
    return people[0]


if __name__ == "__main__":
    m = [[1, 2, 3, 4], [5, 6, 7, 8], [9, 10, 11, 12]]
    print("spiral:", spiral_order(m))
    assert spiral_order(m) == [1, 2, 3, 4, 8, 12, 11, 10, 9, 5, 6, 7]
    assert spiral_order([[1], [2], [3]]) == [1, 2, 3] and spiral_order([]) == []

    sq = [[1, 2, 3], [4, 5, 6], [7, 8, 9]]
    rotate(sq)
    print("rotated:", sq)
    assert sq == [[7, 4, 1], [8, 5, 2], [9, 6, 3]]

    assert reverse_int(123) == 321 and reverse_int(-120) == -21
    assert reverse_int(1534236469) == 0                     # 9646324351 > 2^31 - 1
    assert reverse_int(-2147483412) == -2143847412
    print("reverse 1534236469 ->", reverse_int(1534236469), "(overflow)")

    pts = [(1, 1), (3, 2), (5, 3), (4, 1), (2, 3), (1, 4)]
    assert max_points_on_line(pts) == 4                     # (3,2) (4,1) (2,3) (1,4)
    assert max_points_on_line([(0, 0), (1, 1), (0, 0)]) == 3
    assert max_points_on_line([(0, 0), (0, 5), (0, -3), (1, 1)]) == 3   # vertical line
    print("max collinear:", max_points_on_line(pts))

    assert all(josephus(n, k) == josephus_simulated(n, k) for n in range(1, 30) for k in range(1, 8))
    print("Josephus n=7, k=3 survivor index:", josephus(7, 3))
    assert josephus(7, 3) == 3
    print("all asserts passed")
