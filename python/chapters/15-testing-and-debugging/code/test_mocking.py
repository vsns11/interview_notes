"""unittest.mock: Mock, return_value, side_effect, patch, autospec - plus a
hand-written fake. Test doubles replace slow or external dependencies."""
from unittest.mock import Mock, call, create_autospec, patch

import pytest

import inventory
from inventory import price_in


class FakeRates:
    """A hand-written fake: real (simple) behaviour, no network."""
    def __init__(self, rates):
        self.rates = rates

    def get_rate(self, currency):
        return self.rates[currency]


def test_with_fake():
    assert price_in("EUR", 10, FakeRates({"EUR": 0.9})) == 9.0


def test_with_mock_stub_and_verify():
    client = Mock()
    client.get_rate.return_value = 83.5          # stub the answer
    assert price_in("INR", 2, client) == 167.0
    client.get_rate.assert_called_once_with("INR")   # verify the interaction


def test_side_effect_exception():
    client = Mock()
    client.get_rate.side_effect = TimeoutError("rate service down")
    with pytest.raises(TimeoutError):
        price_in("EUR", 10, client)


def test_side_effect_sequence():
    client = Mock()
    client.get_rate.side_effect = [0.9, -1.0]     # one value per call
    assert price_in("EUR", 10, client) == 9.0
    with pytest.raises(ValueError, match="bad rate"):
        price_in("EUR", 10, client)
    assert client.get_rate.call_args_list == [call("EUR"), call("EUR")]


def test_autospec_catches_typos():
    client = create_autospec(FakeRates, instance=True)
    client.get_rate.return_value = 1.0
    with pytest.raises(AttributeError):
        client.get_rates("EUR")                   # misspelled: plain Mock would allow it


def test_patch_where_it_is_used(tmp_path):
    # inventory.py did `import json`; patch the name inventory.json.dumps
    with patch("inventory.json.dumps", return_value="{}") as fake_dumps:
        inventory.Inventory({"a": 1}).save(tmp_path / "x.json")
    fake_dumps.assert_called_once()
    assert (tmp_path / "x.json").read_text() == "{}"


def test_monkeypatch_env(monkeypatch):
    monkeypatch.setenv("APP_ENV", "test")         # undone automatically
    import os
    assert os.environ["APP_ENV"] == "test"


if __name__ == "__main__":
    raise SystemExit(pytest.main([__file__, "-q"]))
