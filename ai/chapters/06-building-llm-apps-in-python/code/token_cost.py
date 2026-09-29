"""Estimate tokens and money for an LLM feature before you build it.

Real token counts come from the provider (Anthropic: messages.count_tokens;
OpenAI: the tiktoken library for its own models). Here we use offline rules of
thumb so the script runs anywhere, then do the cost arithmetic that matters.
Prices are illustrative, per 1 million tokens, in USD (check the pricing page).
"""
import re

PRICES = {             # (input $/MTok, output $/MTok) - illustrative tiers
    "small": (1.00, 5.00),
    "mid":   (3.00, 15.00),
    "large": (5.00, 25.00),
}
CACHE_WRITE_MULT = 1.25   # writing a prefix into a 5-minute cache costs 1.25x input
CACHE_READ_MULT = 0.10    # reading it back costs 0.1x input


def estimate_tokens(text: str) -> dict:
    words = len(text.split())
    pieces = len(re.findall(r"\w+|[^\w\s]", text))   # words + punctuation marks
    return {"chars/4": round(len(text) / 4), "words*1.33": round(words * 1.33),
            "regex pieces": pieces}


def request_cost(model: str, in_tok: int, out_tok: int) -> float:
    p_in, p_out = PRICES[model]
    return in_tok / 1e6 * p_in + out_tok / 1e6 * p_out


def daily_cost(model, requests, prefix_tok, user_tok, out_tok, cache=False,
               cache_writes=0):
    p_in, p_out = PRICES[model]
    if not cache:
        return requests * request_cost(model, prefix_tok + user_tok, out_tok)
    reads = requests - cache_writes
    prefix_cost = (cache_writes * prefix_tok * CACHE_WRITE_MULT
                   + reads * prefix_tok * CACHE_READ_MULT) / 1e6 * p_in
    rest = requests * (user_tok / 1e6 * p_in + out_tok / 1e6 * p_out)
    return prefix_cost + rest


if __name__ == "__main__":
    sample = ("Retrieval-augmented generation (RAG) looks up relevant passages "
              "first, then asks the model to answer from them. It's cheap, "
              "fresh, and cites sources!")
    print("Token estimates for a", len(sample), "character sentence:")
    for k, v in estimate_tokens(sample).items():
        print(f"  {k:<13} {v}")

    print("\nOne request: 3,500 input tokens, 400 output tokens")
    for m in PRICES:
        print(f"  {m:<6} ${request_cost(m, 3500, 400):.4f}")

    # A support bot: 10,000 requests/day, a 3,000-token fixed prefix
    # (instructions + policy excerpts), 500 tokens of question/history, 400 out.
    N, PREFIX, USER, OUT = 10_000, 3_000, 500, 400
    writes = 24 * 60 // 5          # worst case: cache expires every 5 minutes
    print(f"\nSupport bot, {N:,} requests/day on the 'mid' model:")
    base = daily_cost("mid", N, PREFIX, USER, OUT)
    cached = daily_cost("mid", N, PREFIX, USER, OUT, cache=True, cache_writes=writes)
    print(f"  no caching          ${base:8.2f}/day  (${base * 30:,.0f}/month)")
    print(f"  prefix cached       ${cached:8.2f}/day  (${cached * 30:,.0f}/month)")
    print(f"  saving              {100 * (1 - cached / base):.0f}%")
    print(f"  output share of bill (no cache): "
          f"{100 * N * OUT / 1e6 * PRICES['mid'][1] / base:.0f}%")
    small = daily_cost("small", N, PREFIX, USER, OUT, cache=True, cache_writes=writes)
    print(f"  cached + routed to 'small' model ${small:.2f}/day")
