"""Text (str) vs binary (bytes): code points, UTF-8 encoding, decoding errors, normalization."""
import unicodedata


def code_points() -> None:
    for ch in ["A", "é", "€", "😀"]:
        utf8 = ch.encode("utf-8")
        print(f"{ch!r}: ord={ord(ch):>6}  U+{ord(ch):04X}  utf-8={utf8!r:<22} {len(utf8)} byte(s)")
    print(chr(8364), chr(0x1F600))


def encode_decode() -> None:
    s = "café"
    b = s.encode("utf-8")
    print(type(b), b, "len(str)=", len(s), "len(bytes)=", len(b))
    print(b.decode("utf-8"))
    print(repr(b.decode("latin-1")))                     # wrong codec -> mojibake, no error!
    try:
        b.decode("ascii")
    except UnicodeDecodeError as e:
        print("UnicodeDecodeError:", e)
    print(s.encode("ascii", errors="replace"), s.encode("ascii", errors="ignore"))
    print(b"\xff\xfeok".decode("utf-8", errors="replace"))
    print(b"abc"[0], list(b"hi"), bytes([104, 105]))        # indexing bytes gives ints


def normalization() -> None:
    a = "café"          # é as ONE code point
    b = "café"         # e + combining acute accent
    print(a, b, a == b, len(a), len(b))
    nfc = unicodedata.normalize("NFC", b)
    print("after NFC:", a == nfc, len(nfc))


if __name__ == "__main__":
    for f in (code_points, encode_decode, normalization):
        print(f"--- {f.__name__}")
        f()
