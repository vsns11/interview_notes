"""16.9 MapReduce in miniature: map -> shuffle (group by key) -> reduce, over 3 "nodes".
Real frameworks run each map/reduce task on the machine that stores the data block."""
from collections import defaultdict

blocks = {  # three HDFS-style blocks on three nodes (order-event log lines)
    "node-1": ["view laptop", "view tshirt", "view laptop", "buy laptop"],
    "node-2": ["view laptop", "view book", "buy book"],
    "node-3": ["view laptop", "buy laptop", "view tshirt"],
}

def map_phase(lines):                 # runs locally on each node
    for line in lines:
        action, item = line.split()
        yield (f"{action}:{item}", 1)

def combine(pairs):                   # optional local pre-aggregation (fewer bytes to shuffle)
    acc = defaultdict(int)
    for k, v in pairs:
        acc[k] += v
    return list(acc.items())

mapped = {node: combine(map_phase(lines)) for node, lines in blocks.items()}
for node, pairs in mapped.items():
    print(f"map+combine on {node}: {sorted(pairs)}")

# Shuffle: every key goes to exactly one reducer, chosen by hash(key) % reducers
REDUCERS = 2
def reducer_for(key):
    return sum(map(ord, key)) % REDUCERS   # stable toy hash
shuffled = defaultdict(lambda: defaultdict(list))
for pairs in mapped.values():
    for k, v in pairs:
        shuffled[reducer_for(k)][k].append(v)

for r in range(REDUCERS):
    result = {k: sum(vs) for k, vs in sorted(shuffled[r].items())}
    print(f"reducer {r}: {result}")
