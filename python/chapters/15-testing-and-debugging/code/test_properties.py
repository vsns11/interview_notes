"""Property-based testing with Hypothesis: state rules, let it find inputs."""
import pytest
from hypothesis import given, settings
from hypothesis import strategies as st

from inventory import Inventory, OutOfStock, apply_discount, slugify


@given(st.text())
def test_slugify_is_idempotent(s):
    assert slugify(slugify(s)) == slugify(s)


@given(st.text())
def test_slug_has_only_safe_chars(s):
    assert all(c.isascii() and (c.isalnum() or c == "-") for c in slugify(s))


@given(price=st.floats(0, 1e6), pct=st.floats(0, 100))
def test_discount_never_increases_price(price, pct):
    assert apply_discount(price, pct) <= round(price, 2)


@settings(max_examples=200)
@given(st.lists(st.tuples(st.sampled_from(["add", "remove"]),
                          st.integers(1, 20))))
def test_stock_never_negative(ops):
    inv = Inventory()
    for op, qty in ops:
        try:
            getattr(inv, op)("mug", qty)
        except OutOfStock:
            pass
        assert inv.stock.get("mug", 0) >= 0


if __name__ == "__main__":
    # -W: hypothesis was imported before pytest started, which is harmless
    raise SystemExit(pytest.main([__file__, "-q", "-W", "ignore::pytest.PytestAssertRewriteWarning"]))
