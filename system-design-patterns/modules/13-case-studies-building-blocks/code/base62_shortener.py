"""Base62 short codes for a URL shortener, plus the collision maths for hash-and-truncate.

Run: .venv-books/bin/python base62_shortener.py
"""
import hashlib
import math

ALPHABET = "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ"
BASE = len(ALPHABET)  # 62


def encode(n):
    if n == 0:
        return ALPHABET[0]
    out = []
    while n:
        n, r = divmod(n, BASE)
        out.append(ALPHABET[r])
    return "".join(reversed(out))


def decode(s):
    n = 0
    for ch in s:
        n = n * BASE + ALPHABET.index(ch)
    return n


def hash_code(url, length=7, salt=""):
    """Hash-and-truncate: keep `length` base62 digits of a SHA-256 (h mod 62^length)."""
    h = int.from_bytes(hashlib.sha256((url + salt).encode()).digest()[:8], "big")
    return encode(h % BASE ** length).rjust(length, "0")


if __name__ == "__main__":
    for n in (0, 61, 62, 125_000_000, 3_521_614_606_207):
        code = encode(n)
        assert decode(code) == n
        print(f"{n:>16,} -> {code!r} (len {len(code)})")

    for L in (6, 7, 8):
        print(f"length {L}: {BASE ** L:,} codes")
    need = 100_000_000 * 365 * 10       # 100M links/day for 10 years
    print(f"links in 10 years: {need:,} -> need length {math.ceil(math.log(need, BASE))}")

    # Birthday estimate: expected colliding pairs after n random codes in space S ~ n^2 / (2S)
    S = BASE ** 7
    for n in (1_000_000, 100_000_000, 1_000_000_000):
        p_new = n / S                    # chance the NEXT random code hits an existing one
        print(f"after {n:>13,} links: next 7-char hash collides with p = {p_new:.1e} "
              f"(about 1 in {round(1 / p_new):,})")

    # Hash-and-truncate with retry-on-collision, simulated in a tiny 3-char space (238,328 codes)
    store, retries = {}, 0
    for i in range(50_000):
        url, salt = f"https://example.com/page/{i}", ""
        while True:
            code = hash_code(url, 3, salt)
            if code not in store:
                store[code] = url
                break
            retries += 1
            salt += "!"                  # re-hash with a salt until free
    print(f"3-char demo: 50,000 links stored, {retries:,} collision retries "
          f"(space {BASE ** 3:,}, {50_000 / BASE ** 3:.0%} full)")
