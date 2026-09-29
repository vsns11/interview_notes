"""Back-of-the-envelope cost and capacity model for GenAI features.

All prices are ILLUSTRATIVE round numbers (roughly the shape of public list
prices in 2024-2026). Always re-check current pricing before quoting numbers.

Run:  python cost_model.py      (writes two plots into ../img/)
"""
from dataclasses import dataclass
from pathlib import Path

import matplotlib

matplotlib.use("Agg")
import matplotlib.pyplot as plt

IMG = Path(__file__).resolve().parent.parent / "img"
IMG.mkdir(exist_ok=True)


@dataclass(frozen=True)
class ModelPrice:
    name: str
    usd_per_m_in: float   # dollars per 1 million input tokens
    usd_per_m_out: float  # dollars per 1 million output tokens

    def call_cost(self, tokens_in: int, tokens_out: int) -> float:
        return tokens_in / 1e6 * self.usd_per_m_in + tokens_out / 1e6 * self.usd_per_m_out


LARGE = ModelPrice("large", 3.00, 15.00)
SMALL = ModelPrice("small", 0.25, 1.25)   # ~12x cheaper: an assumption to validate
# A hosted open-weights-class model: the fair API comparison for a self-hosted fleet
MID = ModelPrice("mid (hosted open-weights class)", 0.40, 1.60)


def words_to_tokens(words: int) -> int:
    """English rule of thumb: 1 token ~ 0.75 words  ->  tokens ~ words / 0.75."""
    return round(words / 0.75)


# ---------------------------------------------------------------- 1. token bill
def token_bill() -> None:
    print("1) Token bill: ticket-summary feature")
    t_in = words_to_tokens(3_000)   # a long support thread ~3,000 words
    t_out = words_to_tokens(300)    # a 300-word summary
    per_unit = LARGE.call_cost(t_in, t_out)
    volume = 150_000                # summaries per day
    print(f"   unit: {t_in:,} tokens in, {t_out:,} out -> ${per_unit:.4f} per summary (large)")
    print(f"   x {volume:,}/day = ${per_unit * volume:,.0f}/day = ${per_unit * volume * 365 / 1e6:.2f}M/year")
    small = SMALL.call_cost(t_in, t_out)
    print(f"   same on small model: ${small:.5f} per summary = ${small * volume:,.0f}/day")


# ------------------------------------------------ 2. the chat-history trap
def chat_history(turns: int = 20, sys_tokens: int = 800, user_tokens: int = 60,
                 reply_tokens: int = 250, keep_verbatim: int = 4, summary_tokens: int = 300):
    """Tokens sent per turn if we resend the full history vs. a rolling summary."""
    naive, packed = [], []
    for t in range(1, turns + 1):
        history = (t - 1) * (user_tokens + reply_tokens)
        naive.append(sys_tokens + history + user_tokens)
        recent = min(t - 1, keep_verbatim) * (user_tokens + reply_tokens)
        summ = summary_tokens if t - 1 > keep_verbatim else 0
        packed.append(sys_tokens + summ + recent + user_tokens)
    return naive, packed


def chat_trap() -> None:
    naive, packed = chat_history()
    n_cost = sum(LARGE.call_cost(x, 250) for x in naive)
    p_cost = sum(LARGE.call_cost(x, 250) for x in packed)
    print("\n2) Chat-history trap (20-turn conversation, large model)")
    print(f"   input tokens, turn 1 / 10 / 20 (full history): {naive[0]:,} / {naive[9]:,} / {naive[19]:,}")
    print(f"   total input tokens: full={sum(naive):,}  packed={sum(packed):,}")
    print(f"   cost per conversation: full=${n_cost:.4f}  packed=${p_cost:.4f}  "
          f"(saves {1 - p_cost / n_cost:.0%})")
    plt.figure(figsize=(6.4, 3.6))
    x = range(1, len(naive) + 1)
    plt.plot(x, naive, marker="o", label="resend full history")
    plt.plot(x, packed, marker="s", label="4 verbatim turns + summary")
    plt.xticks(range(1, len(naive) + 1, 2))
    plt.xlabel("turn number")
    plt.ylabel("input tokens sent this turn")
    plt.title("Why long chats get expensive")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(IMG / "chat_history_tokens.png", dpi=130)
    plt.close()


# -------------------------------------------- 3. routing + caching arithmetic
def blended_cost(calls_per_day: float, t_in: int, t_out: int,
                 small_share: float, cache_hit: float) -> float:
    """Cache hits cost ~0 model spend; misses split between small and large."""
    misses = calls_per_day * (1 - cache_hit)
    return misses * (small_share * SMALL.call_cost(t_in, t_out)
                     + (1 - small_share) * LARGE.call_cost(t_in, t_out))


def optimisation_stack() -> None:
    calls = 5_000_000   # chat turns per day
    t_in, t_out = 2_500, 300
    print("\n3) Optimisation stack, 5M chat turns/day (2,500 in / 300 out)")
    rows = [("all large, no cache", 0.0, 0.0),
            ("+ route 70% to small", 0.7, 0.0),
            ("+ 15% semantic-cache hits", 0.7, 0.15)]
    base = None
    for label, share, hit in rows:
        c = blended_cost(calls, t_in, t_out, share, hit)
        base = base or c
        print(f"   {label:<28} ${c:>10,.0f}/day   ({c / base:.0%} of baseline)")


# ---------------------------------------------- 4. GPU fleet vs API crossover
def fleet_size(peak_rps: float, out_tokens: int, node_tok_s: float, headroom: float = 1.25) -> int:
    need = peak_rps * out_tokens / node_tok_s
    return int(-(-need * headroom // 1))     # ceil


def api_vs_selfhost() -> None:
    print("\n4) GPU fleet sizing (self-host) vs API")
    nodes = fleet_size(peak_rps=150, out_tokens=500, node_tok_s=3_000)
    print(f"   peak 150 req/s x 500 out tokens = 75,000 tok/s; / 3,000 per node = 25 -> "
          f"{nodes} nodes with 25% headroom")
    node_hour = 3.0
    ops_per_year = 1_200_000   # on-call engineers + tooling, an assumption
    print(f"   fleet: {nodes} x ${node_hour}/h x 8,760 h = ${nodes * node_hour * 8760:,.0f}/yr "
          f"+ ${ops_per_year:,} ops")
    # sweep daily volume; self-host capacity scales in whole nodes
    vols, api, own = [], [], []
    for m in range(1, 31):
        req_day = m * 1_000_000
        api_year = req_day * MID.call_cost(1_500, 500) * 365
        peak_rps = req_day / 86_400 * 3          # peak = 3x the daily average
        n = max(2, fleet_size(peak_rps, 500, 3_000))
        own_year = n * node_hour * 8760 + ops_per_year
        vols.append(m); api.append(api_year / 1e6); own.append(own_year / 1e6)
    cross = next((v for v, a, o in zip(vols, api, own) if o < a), None)
    print(f"   crossover (self-host cheaper) at ~{cross}M requests/day under these assumptions")
    plt.figure(figsize=(6.4, 3.6))
    plt.plot(vols, api, label="hosted API, same model class")
    plt.step(vols, own, where="post", label="self-hosted fleet + ops")
    plt.xlabel("requests per day (millions)")
    plt.ylabel("cost per year ($M)")
    plt.title("Build vs buy: where the lines cross depends on your assumptions")
    plt.legend()
    plt.grid(alpha=0.3)
    plt.tight_layout()
    plt.savefig(IMG / "api_vs_selfhost.png", dpi=130)
    plt.close()


# ------------------------------------------------- 5. memory and vector storage
def memory_and_storage() -> None:
    print("\n5) Memory and storage")
    for params_b in (8, 70):
        for bits in (16, 8, 4):
            gb = params_b * 1e9 * bits / 8 / 1e9
            print(f"   {params_b:>3}B params @ {bits:>2}-bit -> {gb:6.1f} GB of raw weights")
    chunks, dim = 20_000_000, 1024
    print(f"   {chunks:,} chunks x {dim} dims x 4 bytes = {chunks * dim * 4 / 1e9:.1f} GB of float32 vectors "
          "(before index overhead, metadata and replicas)")


if __name__ == "__main__":
    token_bill()
    chat_trap()
    optimisation_stack()
    api_vs_selfhost()
    memory_and_storage()
    print("\nplots written: img/chat_history_tokens.png, img/api_vs_selfhost.png")
