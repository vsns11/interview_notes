"""A small module under test: a stock inventory with pricing and persistence.

>>> slugify("  Blue Mug (Large) ")
'blue-mug-large'
>>> apply_discount(200.0, 15)
170.0
"""
from __future__ import annotations

import json
import re
from dataclasses import dataclass, field
from pathlib import Path
from typing import Protocol


class OutOfStock(Exception):
    """Raised when removing more units than are available."""


def slugify(name: str) -> str:
    """Lower-case, keep letters/digits, join words with '-'.

    >>> slugify("Hello, World!")
    'hello-world'
    """
    words = re.findall(r"[a-z0-9]+", name.lower())
    return "-".join(words)


def apply_discount(price: float, percent: float) -> float:
    """Return price reduced by percent (0-100), rounded to cents."""
    if not 0 <= percent <= 100:
        raise ValueError(f"percent must be 0-100, got {percent}")
    return round(price * (1 - percent / 100), 2)


class RateClient(Protocol):
    def get_rate(self, currency: str) -> float: ...


@dataclass
class Inventory:
    stock: dict[str, int] = field(default_factory=dict)

    def add(self, sku: str, qty: int) -> None:
        if qty <= 0:
            raise ValueError("qty must be positive")
        self.stock[sku] = self.stock.get(sku, 0) + qty

    def remove(self, sku: str, qty: int) -> None:
        have = self.stock.get(sku, 0)
        if qty > have:
            raise OutOfStock(f"{sku}: want {qty}, have {have}")
        self.stock[sku] = have - qty

    def low_stock(self, threshold: int) -> list[str]:
        """SKUs with quantity <= threshold, alphabetically."""
        return sorted(sku for sku, qty in self.stock.items() if qty <= threshold)

    def total_units(self) -> int:
        return sum(self.stock.values())

    def save(self, path: Path) -> None:
        path.write_text(json.dumps(self.stock, sort_keys=True))

    @classmethod
    def load(cls, path: Path) -> Inventory:
        return cls(json.loads(path.read_text()))


def price_in(currency: str, usd: float, client: RateClient) -> float:
    """Convert a USD price using an external rate service (mocked in tests)."""
    rate = client.get_rate(currency)
    if rate <= 0:
        raise ValueError("bad rate")
    return round(usd * rate, 2)


if __name__ == "__main__":
    import doctest
    inv = Inventory()
    inv.add("mug", 5)
    inv.remove("mug", 2)
    print("stock:", inv.stock, "| units:", inv.total_units())
    print("doctest:", doctest.testmod())
