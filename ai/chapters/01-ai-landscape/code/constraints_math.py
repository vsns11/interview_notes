"""Back-of-envelope maths for three real-world AI constraints:
cost, latency and compounding reliability.

The prices are PLACEHOLDERS for illustration - look up your provider's current
price sheet before using numbers like these in a real plan."""

PRICE_IN_PER_M = 3.00    # $ per 1M input tokens  (placeholder)
PRICE_OUT_PER_M = 15.00  # $ per 1M output tokens (placeholder)


def monthly_cost(requests_per_day: int, tokens_in: int, tokens_out: int) -> float:
    per_request = tokens_in / 1e6 * PRICE_IN_PER_M + tokens_out / 1e6 * PRICE_OUT_PER_M
    return per_request * requests_per_day * 30


def latency_s(tokens_out: int, time_to_first_token_s: float, tokens_per_s: float) -> float:
    return time_to_first_token_s + tokens_out / tokens_per_s


def chain_success(step_accuracy: float, steps: int) -> float:
    return step_accuracy ** steps          # every step must succeed, independently


def main() -> None:
    print("1) Cost: support bot, 2,000-token prompt, 300-token answer")
    for rpd in (1_000, 10_000, 100_000):
        print(f"   {rpd:>7,} requests/day -> ${monthly_cost(rpd, 2_000, 300):>10,.0f} / month")

    print("\n2) Latency: 0.5 s to first token, 60 tokens/s generation")
    for n in (50, 300, 1_000):
        print(f"   {n:>5} output tokens -> {latency_s(n, 0.5, 60):5.1f} s")

    print("\n3) Reliability: probability a multi-step task fully succeeds")
    for acc in (0.99, 0.95, 0.90):
        row = "  ".join(f"{s:>2} steps={chain_success(acc, s):5.1%}" for s in (1, 5, 10, 20))
        print(f"   per-step {acc:.0%}: {row}")


if __name__ == "__main__":
    main()
