"""copy, pprint, enum, uuid, hashlib, json and csv."""
import copy
import csv
import hashlib
import io
import json
import pprint
import uuid
from datetime import date
from enum import Enum, StrEnum, auto

if __name__ == "__main__":
    # copy: shallow vs deep
    cfg = {"hosts": ["a", "b"], "port": 80}
    shallow, deep = copy.copy(cfg), copy.deepcopy(cfg)
    cfg["hosts"].append("c")
    print("shallow sees change:", shallow["hosts"], "| deep:", deep["hosts"])

    # pprint: readable nested data
    nested = {"service": "api", "replicas": 3,
              "env": {"DB_URL": "postgres://db:5432/app", "DEBUG": False},
              "ports": [80, 443], "owners": ["platform-team", "sre"]}
    pprint.pp(nested, width=50)

    # enum
    class Status(StrEnum):
        ACTIVE = auto()
        BANNED = auto()

    class Color(Enum):
        RED = 1
        GREEN = 2
    print("StrEnum:", Status.ACTIVE, Status("banned").name, "| Enum:", Color(2),
          [c.name for c in Color])

    # uuid
    u4 = uuid.uuid4()
    u5 = uuid.uuid5(uuid.NAMESPACE_URL, "https://example.com/a")
    print("uuid4 version:", u4.version, "| uuid5 is deterministic:", u5)

    # hashlib
    print("sha256('hello'):", hashlib.sha256(b"hello").hexdigest()[:16], "...")
    h = hashlib.sha256()
    for chunk in (b"hel", b"lo"):              # streaming = same digest
        h.update(chunk)
    print("streamed equal :", h.hexdigest() == hashlib.sha256(b"hello").hexdigest())
    salt = b"fixed-salt-for-demo"
    key = hashlib.pbkdf2_hmac("sha256", b"s3cret", salt, 100_000)
    print("pbkdf2 key len :", len(key))

    # json recap: custom default for unsupported types
    rec = {"id": 7, "when": date(2026, 9, 28), "tags": ("a", "b")}
    text = json.dumps(rec, default=str, sort_keys=True)
    print("json:", text, "| back:", json.loads(text)["tags"])

    # csv recap: DictWriter / DictReader
    buf = io.StringIO()
    w = csv.DictWriter(buf, fieldnames=["name", "city"])
    w.writeheader()
    w.writerows([{"name": "Asha", "city": "Pune"}, {"name": "Li, Wei", "city": "Oslo"}])
    print("csv text:", buf.getvalue().splitlines())
    print("csv read:", list(csv.DictReader(io.StringIO(buf.getvalue())))[1])
