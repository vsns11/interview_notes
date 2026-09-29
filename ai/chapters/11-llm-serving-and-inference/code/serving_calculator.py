"""Back-of-the-envelope calculator for LLM serving: GPU memory, KV cache, token bills, fleets.

All prices and throughputs are ILLUSTRATIVE assumptions for practice, not quotes.
Model shapes are the published configs of the open Llama models.
"""
from dataclasses import dataclass
import math

GB = 1e9


# ------------------------------------------------------------------ memory
@dataclass
class ModelShape:
    name: str
    params: float        # number of parameters
    layers: int
    kv_heads: int        # = attention heads for MHA, fewer for GQA/MQA
    head_dim: int


LLAMA2_7B = ModelShape("Llama-2-7B (MHA)", 6.7e9, 32, 32, 128)
LLAMA3_8B = ModelShape("Llama-3-8B (GQA)", 8.0e9, 32, 8, 128)
LLAMA3_70B = ModelShape("Llama-3-70B (GQA)", 70.6e9, 80, 8, 128)


def weight_bytes(m: ModelShape, bytes_per_param: float) -> float:
    return m.params * bytes_per_param


def kv_bytes_per_token(m: ModelShape, kv_bytes: float = 2) -> float:
    # 2 tensors (K and V) x layers x kv_heads x head_dim x bytes per number
    return 2 * m.layers * m.kv_heads * m.head_dim * kv_bytes


def fit(m, n_gpus, gpu_gb, bytes_per_param, ctx_len, util=0.90, kv_bytes=2):
    """How many sequences of ctx_len tokens fit next to the weights?
    util = fraction of GPU memory the engine may use (vLLM's default is 0.9);
    activations/buffers are assumed to come out of the remaining 10 %."""
    usable = n_gpus * gpu_gb * GB * util
    w = weight_bytes(m, bytes_per_param)
    free = usable - w
    per_tok = kv_bytes_per_token(m, kv_bytes)
    if free <= 0:
        return w, 0, 0
    tokens = free / per_tok
    return w, tokens, int(tokens // ctx_len)


# ------------------------------------------------------------------ money
@dataclass
class Price:
    inp: float   # $ per 1M input tokens
    out: float   # $ per 1M output tokens


FRONTIER = Price(3.0, 12.0)          # assumed large hosted model
SMALL = Price(0.3, 1.2)              # assumed 10x cheaper small model


def request_cost(p: Price, tin, tout, cached_frac=0.0, cached_discount=0.9):
    inp = tin * p.inp / 1e6
    inp *= (1 - cached_frac * cached_discount)
    return inp + tout * p.out / 1e6


def fleet(peak_out_tok_s, node_tok_s, headroom_nodes, node_price_hr):
    base = math.ceil(peak_out_tok_s / node_tok_s)
    nodes = base + headroom_nodes
    return base, nodes, nodes * node_price_hr * 24 * 365


def banner(t):
    print("\n" + t + "\n" + "-" * len(t))


if __name__ == "__main__":
    banner("1. KV cache per token (fp16)")
    for m in (LLAMA2_7B, LLAMA3_8B, LLAMA3_70B):
        b = kv_bytes_per_token(m)
        print(f"{m.name:<20} 2 x {m.layers} x {m.kv_heads} x {m.head_dim} x 2 B = "
              f"{b:>9,.0f} B = {b / 1024:5.0f} KiB/token -> {b * 4096 / 2**30:5.2f} GiB per 4K-token sequence")

    banner("2. Does it fit? (usable = 90% of GPU memory, 4K-token sequences)")
    cases = [
        (LLAMA3_8B, 1, 24, 2, "fp16 on 1 x 24 GB"),
        (LLAMA3_8B, 1, 80, 2, "fp16 on 1 x 80 GB"),
        (LLAMA3_70B, 2, 80, 2, "fp16 on 2 x 80 GB"),
        (LLAMA3_70B, 4, 80, 2, "fp16 on 4 x 80 GB"),
        (LLAMA3_70B, 2, 80, 1, "int8 on 2 x 80 GB"),
        (LLAMA3_70B, 1, 80, 0.5, "int4 on 1 x 80 GB"),
    ]
    for m, n, g, bpp, label in cases:
        w, toks, seqs = fit(m, n, g, bpp, 4096)
        print(f"{m.name:<20} {label:<20} weights {w / GB:6.1f} GB | KV room "
              f"{toks:>9,.0f} tokens = {seqs:4d} concurrent 4K sequences")

    banner("3. Token bill: one request, one conversation, one month")
    c = request_cost(FRONTIER, 3000, 300)
    print(f"one turn, 3,000 in + 300 out on frontier: ${c:.4f}")
    conv = request_cost(FRONTIER, 30_000, 3_000)
    print(f"support bot: 10 turns = 30K in + 3K out -> ${conv:.3f}/conversation")
    print(f"  x 100K conversations/day = ${conv * 1e5:,.0f}/day, ${conv * 1e5 * 30:,.0f}/30 days")

    banner("4. Chat history trap (history re-sent every turn)")
    system, user, answer = 500, 200, 200
    total_in = 0
    for turn in range(1, 11):
        tin = system + turn * user + (turn - 1) * answer
        total_in += tin
    naive = 10 * (system + user)
    print(f"10 turns: input tokens with full history = {total_in:,} "
          f"vs {naive:,} if every turn were independent ({total_in / naive:.1f}x)")

    banner("5. Document summarization")
    words_in, words_out = 5000, 400
    tin, tout = round(words_in / 0.75, -2), round(words_out / 0.75, -2)
    per_doc = request_cost(FRONTIER, tin, tout)
    print(f"10 pages ~ {words_in:,} words ~ {tin:,.0f} tokens in; {words_out}-word summary ~ {tout:,.0f} tokens out")
    print(f"per doc = ${per_doc:.4f}; x 200K/day = ${per_doc * 2e5:,.0f}/day "
          f"= ${per_doc * 2e5 * 365 / 1e6:.2f}M/year")

    banner("6. GPU fleet for self-hosting")
    peak_tok = 100 * 400
    base, nodes, yearly = fleet(peak_tok, 2500, 4, 3.0)
    print(f"peak 100 req/s x 400 out = {peak_tok:,} out tok/s -> {base} nodes + 4 headroom = {nodes}")
    print(f"at $3/node-hour: ${yearly:,.0f}/year (before network, storage, people)")

    banner("7. Vector store for RAG (float32)")
    for n in (10e6, 1e9):
        print(f"{n:>13,.0f} chunks x 768 dims x 4 B = {n * 768 * 4 / GB:8.2f} GB raw vectors")

    banner("8. Capstone: AI assistant for a 5M-DAU SaaS")
    inter = 5e6 * 0.20 * 4
    avg_s = inter / 86400
    print(f"interactions/day = 5M x 20% x 4 = {inter / 1e6:.0f}M  (avg {avg_s:.1f}/s, x3 peak = {3 * avg_s:.0f}/s)")
    tin_day, tout_day = inter * 2500, inter * 300
    inp_cost, out_cost = tin_day * FRONTIER.inp / 1e6, tout_day * FRONTIER.out / 1e6
    naive = inp_cost + out_cost
    print(f"naive: {tin_day / 1e9:.0f}B in -> ${inp_cost:,.0f} + {tout_day / 1e9:.1f}B out -> ${out_cost:,.0f}"
          f" = ${naive:,.0f}/day (${naive * 365 / 1e6:.2f}M/yr)")
    print(f"  per active user-day ${naive / 1e6:.4f}; 30 days = ${naive / 1e6 * 30:.2f} "
          f"= {naive / 1e6 * 30 / 20:.1%} of a $20 seat")
    blend = 0.75 * 0.1 + 0.25
    routed_in, routed_out = inp_cost * blend, out_cost * blend
    print(f"route 75% to small (1/10 price): x{blend} -> ${routed_in + routed_out:,.2f}/day")
    prefixed = routed_in * (1 - 0.5 * 0.9) + routed_out
    print(f"prefix cache (50% input hits, 90% off): ${prefixed:,.2f}/day")
    final = prefixed * 0.85
    print(f"result cache (15% hits skip the model): ${final:,.2f}/day = ${final * 365 / 1e6:.2f}M/yr, "
          f"{1 - final / naive:.1%} below naive")
    print(f"  sensitivity: 25% result-cache hits -> ${prefixed * 0.75:,.2f}/day")

    small_calls = inter * 0.75 * 0.85
    out_tok_s = small_calls * 300 / 86400
    base, nodes, _ = fleet(3 * out_tok_s, 2500, 3, 3.0)
    fleet_day = nodes * 3 * 24
    small_api = (small_calls * 2500 * SMALL.inp / 1e6) * 0.55 + small_calls * 300 * SMALL.out / 1e6
    print(f"small path: {small_calls / 1e6:.2f}M calls/day, {out_tok_s:,.0f} out tok/s avg, "
          f"{3 * out_tok_s:,.0f} peak -> {base} nodes, provision {nodes}")
    print(f"  self-host ${fleet_day:,.0f}/day vs hosted small model ${small_api:,.2f}/day "
          f"-> saves ${(small_api - fleet_day) * 365 / 1e3:,.0f}K/yr before people and ops")

    # plot: total memory for Llama-3-70B fp16 vs number of concurrent 8K-token sequences
    import matplotlib
    matplotlib.use("Agg")
    import matplotlib.pyplot as plt
    from pathlib import Path
    out = Path(__file__).resolve().parent.parent / "img"
    out.mkdir(exist_ok=True)
    fig, ax = plt.subplots(figsize=(7, 3.8))
    users = list(range(0, 129, 8))
    for m, bpp, label, color in [(LLAMA3_70B, 2, "70B fp16", "#1f77b4"),
                                 (LLAMA3_70B, 1, "70B int8", "#ff7f0e"),
                                 (LLAMA3_8B, 2, "8B fp16", "#2ca02c")]:
        total = [(weight_bytes(m, bpp) + u * 8192 * kv_bytes_per_token(m)) / GB for u in users]
        ax.plot(users, total, marker="o", ms=3, label=label, color=color)
    for gpus in (1, 2, 4):
        ax.axhline(gpus * 80 * 0.9, ls="--", lw=0.8, color="grey")
        ax.text(92, gpus * 72 + 3, f"{gpus} x 80 GB (90% usable)", fontsize=8, color="grey")
    ax.set_xlabel("concurrent sequences of 8,192 tokens")
    ax.set_ylabel("GB (weights + fp16 KV cache)")
    ax.set_title("Weights are fixed; KV cache grows with users x context")
    ax.legend(loc="upper left", bbox_to_anchor=(0.0, 0.93))
    fig.tight_layout()
    fig.savefig(out / "memory_vs_concurrency.png", dpi=130)
    print("\nsaved img/memory_vs_concurrency.png")
