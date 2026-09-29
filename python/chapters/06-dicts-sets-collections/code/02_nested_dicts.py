"""Nested dictionaries: build, read, update, walk safely, and the JSON connection."""
import json


def build_family() -> dict:
    return {
        "name": "John Doe",
        "children": {
            "Alice": {"age": 10, "gender": "Female"},
            "Bob": {"age": 8, "gender": "Male"},
        },
    }


def access_modify() -> None:
    family = build_family()
    print("Parent:", family["name"])
    print("Alice's age:", family["children"]["Alice"]["age"])
    family["children"]["Alice"]["age"] = 11                  # update deep value
    family["children"]["Cara"] = {"age": 3, "gender": "Female"}  # add a new branch
    for child, info in family["children"].items():
        print(f"  {child}: {info['age']} ({info['gender']})")


def safe_access() -> None:
    family = build_family()
    # chained .get() with {} defaults never raises
    print(family.get("children", {}).get("Zed", {}).get("age", "unknown"))

    def deep_get(d, *keys, default=None):
        for k in keys:
            if not isinstance(d, dict) or k not in d:
                return default
            d = d[k]
        return d
    print(deep_get(family, "children", "Bob", "age"), deep_get(family, "x", "y", default=-1))


def json_roundtrip() -> None:
    family = build_family()
    text = json.dumps(family, indent=2)
    print(text[:80] + " ...")
    back = json.loads(text)
    print(back == family, type(back["children"]))
    print(json.loads('{"1": [1, 2]}'), json.dumps({1: (1, 2)}))  # keys -> str, tuples -> lists


if __name__ == "__main__":
    for f in (access_modify, safe_access, json_roundtrip):
        print(f"--- {f.__name__}")
        f()
