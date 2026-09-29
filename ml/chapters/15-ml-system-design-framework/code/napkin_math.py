"""Back-of-the-envelope estimates you say out loud in an ML/AI design interview.

All prices and rates are ASSUMPTIONS for practice, not quotes for any real provider.
"""

SECONDS_PER_DAY = 86_400


def qps(per_day, peak_factor=3.0):
    avg = per_day / SECONDS_PER_DAY
    return avg, avg * peak_factor


def model_cost_per_day(requests_per_day, share, calls_per_request, cost_per_call):
    return requests_per_day * share * calls_per_request * cost_per_call


if __name__ == "__main__":
    # 1) traffic: 10M prediction requests/day, peak = 3x average (assumed)
    avg, peak = qps(10_000_000)
    print(f"10M req/day -> avg {avg:,.0f} QPS, peak ~{peak:,.0f} QPS (3x peak factor assumed)")

    # 2) every request calls a model at $0.01
    c = model_cost_per_day(10_000_000, 1.0, 1, 0.01)
    print(f"all requests x $0.01        -> ${c:,.0f}/day, ${c * 30:,.0f}/30-day month")

    # 3) routing: only 20% of requests go to the large model (share must be MEASURED, not assumed)
    c = model_cost_per_day(10_000_000, 0.20, 1, 0.01)
    print(f"20% to large model x $0.01  -> ${c:,.0f}/day (excludes small model, retrieval, retries)")

    # 4) support assistant: 500k orders/day, 8% contact rate, 10 calls/conversation at $0.01
    conv = 500_000 * 0.08
    per_conv = 10 * 0.01
    print(f"support: {conv:,.0f} conversations/day x ${per_conv:.2f} = ${conv * per_conv:,.0f}/day model cost")
    print(f"  compare per resolved issue, not per call: human ticket assumed $4.00")
    for resolve_rate in (0.3, 0.5, 0.7):
        # AI conversation cost + human cost for the ones it could not resolve
        cost_ai_path = per_conv + (1 - resolve_rate) * 4.00
        print(f"  resolve {resolve_rate:.0%}: expected cost/ticket ${cost_ai_path:.2f} vs $4.00 human-only")

    # 5) batch precompute: 25M users x 500 recs = rows stored; wasted if most users don't visit
    rows = 25_000_000 * 500
    print(f"precompute 25M users x 500 recs = {rows / 1e9:.1f}B rows; "
          f"active-only (5M) = {5_000_000 * 500 / 1e9:.1f}B rows")
