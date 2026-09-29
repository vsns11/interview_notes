"""19.12 Bit tricks and number theory helpers."""
import math
from functools import reduce


def single_number(nums: list[int]) -> int:
    """Every value appears twice except one: x ^ x == 0, x ^ 0 == x."""
    return reduce(lambda a, b: a ^ b, nums, 0)


def is_power_of_two(n: int) -> bool:
    return n > 0 and n & (n - 1) == 0      # a power of two has exactly one 1-bit


def count_bits_table(n: int) -> list[int]:
    """popcount for 0..n in O(n): bits(i) = bits(i >> 1) + (i & 1)."""
    out = [0] * (n + 1)
    for i in range(1, n + 1):
        out[i] = out[i >> 1] + (i & 1)
    return out


def subsets_by_mask(items: list[str]) -> list[list[str]]:
    n = len(items)
    return [[items[i] for i in range(n) if mask >> i & 1] for mask in range(1 << n)]


def sieve(n: int) -> list[int]:
    is_p = [True] * (n + 1)
    is_p[0:2] = [False, False]
    for i in range(2, math.isqrt(n) + 1):
        if is_p[i]:
            is_p[i * i:: i] = [False] * len(range(i * i, n + 1, i))
    return [i for i, p in enumerate(is_p) if p]


def fast_pow(base: int, exp: int, mod: int) -> int:
    """Square-and-multiply - what pow(base, exp, mod) does for you."""
    result = 1 % mod                  # so that mod == 1 gives 0, like pow()
    base %= mod
    while exp:
        if exp & 1:
            result = result * base % mod
        base = base * base % mod
        exp >>= 1
    return result


if __name__ == "__main__":
    x = 0b1011_0100
    print(f"x = {x} = {x:#010b}")
    print("lowest set bit x & -x :", x & -x)
    print("clear lowest bit x & (x-1):", bin(x & (x - 1)))
    print("popcount bin(x).count('1'):", bin(x).count("1"), "| int.bit_count():", x.bit_count())
    print("bit_length:", x.bit_length(), "| test bit 2:", x >> 2 & 1, "| set bit 0:", bin(x | 1))
    print("~5 =", ~5, " (two's complement: -(5+1))")
    print("-7 >> 1 =", -7 >> 1, " (arithmetic shift floors)")

    assert single_number([4, 1, 2, 1, 2]) == 4
    assert [is_power_of_two(n) for n in (0, 1, 6, 64)] == [False, True, False, True]
    assert count_bits_table(5) == [0, 1, 1, 2, 1, 2]
    assert len(subsets_by_mask(["a", "b", "c"])) == 8
    a, b = 3, 5
    a ^= b; b ^= a; a ^= b          # XOR swap (party trick - just use a, b = b, a)
    assert (a, b) == (5, 3)

    MOD = 10**9 + 7
    print("gcd(84, 36) =", math.gcd(84, 36), "| lcm(4, 6) =", math.lcm(4, 6))
    print("pow(2, 100, MOD) =", pow(2, 100, MOD))
    assert fast_pow(2, 100, MOD) == pow(2, 100, MOD)
    inv3 = pow(3, -1, MOD)           # modular inverse (Python 3.8+)
    assert 3 * inv3 % MOD == 1
    print("comb(5,2) =", math.comb(5, 2), "| perm(5,2) =", math.perm(5, 2),
          "| factorial(5) =", math.factorial(5))
    print("isqrt(10**20 + 1) =", math.isqrt(10**20 + 1))
    print("primes <= 30:", sieve(30))
    assert sieve(30) == [2, 3, 5, 7, 11, 13, 17, 19, 23, 29]
    print("2**100 =", 2**100, "(arbitrary precision)")
    print("divmod(-7, 2) =", divmod(-7, 2), "| math.fmod(-7, 2) =", math.fmod(-7, 2))
    print("inf sentinels:", float("inf") > 10**100, "| math.inf == float('inf'):", math.inf == float("inf"))
    print("all asserts passed")
