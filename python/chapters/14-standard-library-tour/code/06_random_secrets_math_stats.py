"""random (simulation), secrets (security), math, statistics."""
import math
import random
import secrets
import statistics

if __name__ == "__main__":
    rng = random.Random(42)                    # own seeded generator
    print("randint      :", [rng.randint(1, 6) for _ in range(5)])
    print("choice       :", rng.choice(["red", "green", "blue"]))
    print("sample       :", rng.sample(range(100), 3))
    deck = list(range(10)); rng.shuffle(deck)
    print("shuffle      :", deck)
    print("weighted     :", rng.choices("ABC", weights=[70, 20, 10], k=8))
    print("gauss        :", round(rng.gauss(0, 1), 4))

    token = secrets.token_urlsafe(16)
    print("token_urlsafe:", len(token), "chars")
    print("token_hex(8) :", len(secrets.token_hex(8)), "hex chars")
    print("compare_digest:", secrets.compare_digest("abc", "abc"))

    print("isclose      :", 0.1 + 0.2 == 0.3, math.isclose(0.1 + 0.2, 0.3))
    print("floor/ceil   :", math.floor(-2.5), math.ceil(-2.5), round(2.5), round(3.5))
    print("sqrt, hypot  :", math.sqrt(16), math.hypot(3, 4))
    print("comb, perm   :", math.comb(5, 2), math.perm(5, 2), math.factorial(5))
    print("gcd, lcm     :", math.gcd(12, 18), math.lcm(4, 6))
    print("log          :", math.log(8, 2), math.log10(1000), round(math.e, 5))
    print("fsum vs sum  :", sum([0.1] * 10), math.fsum([0.1] * 10))

    data = [2, 4, 4, 4, 5, 5, 7, 9]
    print("mean/median  :", statistics.mean(data), statistics.median(data))
    print("mode         :", statistics.mode(data))
    print("pstdev/stdev :", statistics.pstdev(data), round(statistics.stdev(data), 4))
    print("quantiles    :", statistics.quantiles(data, n=4))
    x, y = [1, 2, 3, 4, 5], [2, 4, 5, 4, 5]
    print("correlation  :", round(statistics.correlation(x, y), 4))
    print("linear_reg   :", statistics.linear_regression(x, y))
