"""Shared pytest fixtures and settings - pytest loads this file automatically."""
import pytest

from inventory import Inventory


def pytest_configure(config):
    config.addinivalue_line("markers", "slow: marks a test as slow")


@pytest.fixture
def inv():
    """A fresh inventory with two products for every test that asks for it."""
    inventory = Inventory()
    inventory.add("mug", 5)
    inventory.add("pen", 10)
    return inventory


if __name__ == "__main__":
    print("conftest.py is loaded by pytest; run: pytest -q")
