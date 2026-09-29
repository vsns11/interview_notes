"""TDD example: these tests were written BEFORE Inventory.low_stock existed."""
import pytest


def test_low_stock_lists_skus_below_threshold(inv):      # inv: mug=5, pen=10
    assert inv.low_stock(threshold=6) == ["mug"]


def test_low_stock_sorted_and_inclusive(inv):
    inv.add("cap", 1)
    assert inv.low_stock(threshold=10) == ["cap", "mug", "pen"]


if __name__ == "__main__":
    raise SystemExit(pytest.main([__file__, "-q"]))
