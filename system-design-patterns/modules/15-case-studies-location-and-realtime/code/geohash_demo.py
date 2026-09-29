"""Geohash encoding + "nearby" search, written from the public geohash spec
(Niemeyer, 2008; see geohash.org / Wikipedia "Geohash").

What it shows:
  1. encode(lat, lon, precision) -> base-32 string; each extra character
     shrinks the cell ~32x (alternating longitude/latitude bit splits).
  2. Nearby points usually share a prefix -> a prefix scan is a range query.
  3. The boundary problem: two points 20 m apart can share NO prefix.
  4. The fix: search the cell + its 8 neighbours, then filter by exact distance.

Run:  .venv-books/bin/python geohash_demo.py
"""
import math
import random

BASE32 = "0123456789bcdefghjkmnpqrstuvwxyz"  # no a, i, l, o


def encode(lat: float, lon: float, precision: int = 6) -> str:
    lat_rng, lon_rng = [-90.0, 90.0], [-180.0, 180.0]
    bits, bit_count, even, out = 0, 0, True, []
    while len(out) < precision:
        rng, val = (lon_rng, lon) if even else (lat_rng, lat)  # lon bit first
        mid = (rng[0] + rng[1]) / 2
        if val >= mid:
            bits = (bits << 1) | 1
            rng[0] = mid
        else:
            bits <<= 1
            rng[1] = mid
        even = not even
        bit_count += 1
        if bit_count == 5:            # 5 bits -> one base-32 character
            out.append(BASE32[bits])
            bits, bit_count = 0, 0
    return "".join(out)


def decode_bbox(gh: str):
    """Return (lat_min, lat_max, lon_min, lon_max) of a geohash cell."""
    lat_rng, lon_rng, even = [-90.0, 90.0], [-180.0, 180.0], True
    for ch in gh:
        v = BASE32.index(ch)
        for shift in range(4, -1, -1):
            rng = lon_rng if even else lat_rng
            mid = (rng[0] + rng[1]) / 2
            if (v >> shift) & 1:
                rng[0] = mid
            else:
                rng[1] = mid
            even = not even
    return lat_rng[0], lat_rng[1], lon_rng[0], lon_rng[1]


def neighbours(gh: str):
    """The 8 surrounding cells: step one cell-size from the centre and re-encode."""
    la0, la1, lo0, lo1 = decode_bbox(gh)
    clat, clon, dlat, dlon = (la0 + la1) / 2, (lo0 + lo1) / 2, la1 - la0, lo1 - lo0
    out = []
    for dy in (-1, 0, 1):
        for dx in (-1, 0, 1):
            if dx == dy == 0:
                continue
            lat = clat + dy * dlat
            if not -90 < lat < 90:
                continue                           # nothing beyond the poles
            lon = (clon + dx * dlon + 540) % 360 - 180  # wrap the antimeridian
            out.append(encode(lat, lon, len(gh)))
    return out


def haversine_m(lat1, lon1, lat2, lon2):
    r = 6_371_000.0
    p1, p2 = math.radians(lat1), math.radians(lat2)
    dp, dl = p2 - p1, math.radians(lon2 - lon1)
    a = math.sin(dp / 2) ** 2 + math.cos(p1) * math.cos(p2) * math.sin(dl / 2) ** 2
    return 2 * r * math.asin(math.sqrt(a))


def cell_size_m(precision, at_lat=0.0):
    la0, la1, lo0, lo1 = decode_bbox(encode(at_lat, 0.0, precision))
    h = haversine_m(la0, 0, la1, 0)
    w = haversine_m(at_lat, lo0, at_lat, lo1)
    return w, h


# ---------------------------------------------------------------- demo
if __name__ == "__main__":
    print("1) Cell size per precision (at latitude 40):")
    for p in range(4, 9):
        w, h = cell_size_m(p, 40.0)
        print(f"   precision {p}: ~{w:>9,.0f} m wide x {h:>8,.0f} m tall")

    home = (40.7484, -73.9857)   # an arbitrary point in a big city
    gh = encode(*home, 9)
    print(f"\n2) encode{home} -> {gh}   (prefixes: {gh[:4]} > {gh[:5]} > {gh[:6]})")
    la0, la1, lo0, lo1 = decode_bbox(gh[:6])
    assert la0 <= home[0] <= la1 and lo0 <= home[1] <= lo1

    # boundary problem: find two points ~20 m apart straddling a precision-6 edge
    edge_lon = decode_bbox(gh[:6])[3]          # east edge of the 6-char cell
    a, b = (home[0], edge_lon - 0.0001), (home[0], edge_lon + 0.0001)
    print(f"\n3) Boundary problem: {encode(*a, 6)} vs {encode(*b, 6)}"
          f"  -> {haversine_m(*a, *b):.0f} m apart, different cells")
    assert encode(*a, 6) != encode(*b, 6)
    assert encode(*b, 6) in neighbours(encode(*a, 6))

    # build an index: geohash-6 -> list of businesses (a dict stands in for a DB index)
    rnd = random.Random(15)
    places = [(f"biz{i:04d}", home[0] + rnd.uniform(-0.05, 0.05),
               home[1] + rnd.uniform(-0.05, 0.05)) for i in range(5000)]
    index = {}
    for pid, lat, lon in places:
        index.setdefault(encode(lat, lon, 6), []).append((pid, lat, lon))

    radius = 500
    cells = [encode(*home, 6)] + neighbours(encode(*home, 6))
    candidates = [p for c in cells for p in index.get(c, [])]
    hits = sorted((haversine_m(*home, lat, lon), pid) for pid, lat, lon in candidates
                  if haversine_m(*home, lat, lon) <= radius)
    brute = sorted((haversine_m(*home, lat, lon), pid) for pid, lat, lon in places
                   if haversine_m(*home, lat, lon) <= radius)
    only_own = [p for p in index.get(encode(*home, 6), [])
                if haversine_m(*home, p[1], p[2]) <= radius]
    print(f"\n4) Nearby search, radius {radius} m, over {len(places):,} places")
    print(f"   cells scanned     : 9 ({', '.join(cells[:3])}, ...)")
    print(f"   candidates read   : {len(candidates)}  ({len(candidates)/len(places):.1%} of all)")
    print(f"   within {radius} m      : {len(hits)}  (brute force agrees: {hits == brute})")
    print(f"   own cell only     : {len(only_own)}  <- misses {len(hits)-len(only_own)} if you skip neighbours")
    for d, pid in hits[:3]:
        print(f"     {pid}  {d:6.0f} m")
    assert hits == brute
    print("\nall asserts passed")
