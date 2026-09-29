"""pytest style: plain asserts, fixtures, parametrize, tmp_path, marks."""
import time

import pytest

from inventory import Inventory, OutOfStock, apply_discount, slugify


def test_total_units(inv):                 # 'inv' fixture comes from conftest.py
    assert inv.total_units() == 15


def test_remove_reduces_stock(inv):
    inv.remove("pen", 4)
    assert inv.stock == {"mug": 5, "pen": 6}


def test_remove_too_many_raises(inv):
    with pytest.raises(OutOfStock, match="want 6, have 5"):
        inv.remove("mug", 6)


@pytest.mark.parametrize(
    ("price", "percent", "expected"),
    [(100, 0, 100), (100, 25, 75), (19.99, 10, 17.99), (50, 100, 0)],
)
def test_discount(price, percent, expected):
    assert apply_discount(price, percent) == pytest.approx(expected)


@pytest.mark.parametrize("bad", [-1, 101])
def test_discount_rejects_bad_percent(bad):
    with pytest.raises(ValueError):
        apply_discount(10, bad)


@pytest.mark.parametrize(("raw", "slug"), [
    ("Blue Mug", "blue-mug"),
    ("  --Hello__World-- ", "hello-world"),
    ("", ""),
], ids=["simple", "punctuation", "empty"])
def test_slugify(raw, slug):
    assert slugify(raw) == slug


def test_save_and_load_roundtrip(inv, tmp_path):
    path = tmp_path / "stock.json"          # unique temp dir per test
    inv.save(path)
    assert Inventory.load(path) == inv
    assert path.read_text() == '{"mug": 5, "pen": 10}'


@pytest.fixture
def audit_log(tmp_path):
    """Fixture with teardown: code after 'yield' runs after the test."""
    f = (tmp_path / "audit.txt").open("w")
    yield f
    f.close()


def test_audit_written(audit_log):
    audit_log.write("added mug\n")
    assert not audit_log.closed


@pytest.mark.slow
def test_slow_path():
    time.sleep(0.2)
    assert True


@pytest.mark.xfail(reason="float rounding: 2.675 is stored as 2.67499...; use Decimal",
                   strict=True)
def test_known_bug():
    assert apply_discount(2.675, 0) == 2.68


if __name__ == "__main__":
    raise SystemExit(pytest.main([__file__, "-q"]))
