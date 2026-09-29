"""collections: Counter, defaultdict, OrderedDict, deque, ChainMap, namedtuple."""
from collections import ChainMap, Counter, OrderedDict, defaultdict, deque, namedtuple


def counter() -> None:
    words = "the cat and the hat and the bat".split()
    c = Counter(words)
    print(c)
    print(c.most_common(2), c["the"], c["dog"])          # missing key -> 0, no KeyError
    c.update(["cat", "cat"]); c.subtract(["the"])
    print(c.most_common(3))
    a, b = Counter("aabbb"), Counter("abccc")
    print(a + b, a - b, a & b, a | b, sep=" | ")
    print(Counter("mississippi").total())                # 3.10+


def default_dict() -> None:
    pairs = [("fruit", "apple"), ("veg", "kale"), ("fruit", "fig")]
    groups: defaultdict[str, list[str]] = defaultdict(list)
    for kind, name in pairs:
        groups[kind].append(name)                        # missing key -> list() created
    print(dict(groups))
    counts = defaultdict(int)
    for ch in "banana":
        counts[ch] += 1
    print(dict(counts))
    print("veg" in groups, "meat" in groups, groups.get("meat"))   # get/in don't create keys
    _ = groups["meat"]                                               # [] access DOES
    print("after groups['meat']:", dict(groups))
    tree = lambda: defaultdict(tree)                                # autovivifying tree
    t = tree(); t["a"]["b"]["c"] = 1
    print(t["a"]["b"]["c"])


def ordered_dict() -> None:
    od = OrderedDict.fromkeys("abcde")
    od.move_to_end("b")                   # to the end
    od.move_to_end("e", last=False)       # to the front
    print(list(od))
    print(od.popitem(last=False))         # FIFO pop
    print(OrderedDict(a=1, b=2) == OrderedDict(b=2, a=1), dict(a=1, b=2) == dict(b=2, a=1))


class LRUCache:
    """Least-recently-used cache in a few lines with OrderedDict."""
    def __init__(self, capacity: int):
        self.capacity, self.data = capacity, OrderedDict()

    def get(self, key):
        if key not in self.data:
            return None
        self.data.move_to_end(key)
        return self.data[key]

    def put(self, key, value):
        self.data[key] = value
        self.data.move_to_end(key)
        if len(self.data) > self.capacity:
            self.data.popitem(last=False)


def lru_demo() -> None:
    cache = LRUCache(2)
    cache.put("a", 1); cache.put("b", 2); cache.get("a"); cache.put("c", 3)
    print("LRU keeps:", list(cache.data))   # 'b' was least recently used


def deque_demo() -> None:
    q = deque([1, 2, 3])
    q.append(4); q.appendleft(0)
    print("after append/appendleft:", q)
    left, right = q.popleft(), q.pop()
    print("popleft ->", left, "| pop ->", right, "| now", q)
    q.rotate(1); print("rotate(1):", q)
    last3 = deque(maxlen=3)
    for x in range(6):
        last3.append(x)                   # old items fall off the other end
    print("maxlen=3:", last3)


def chain_map() -> None:
    defaults = {"color": "red", "user": "guest", "debug": False}
    env = {"user": "admin"}
    cli = {"debug": True}
    cfg = ChainMap(cli, env, defaults)    # first mapping wins
    print(cfg["color"], cfg["user"], cfg["debug"])
    cfg["color"] = "blue"                 # writes go to the FIRST mapping only
    print(cli, defaults["color"])
    child = cfg.new_child({"user": "tmp"})
    print(child["user"], cfg["user"])


def named_tuple() -> None:
    Card = namedtuple("Card", "rank suit")
    hand = [Card("A", "spades"), Card("10", "hearts")]
    print(hand[0].rank, hand[1], Card._fields)


if __name__ == "__main__":
    for f in (counter, default_dict, ordered_dict, lru_demo, deque_demo, chain_map, named_tuple):
        print(f"--- {f.__name__}")
        f()
