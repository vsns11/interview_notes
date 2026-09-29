"""Chapter 9 - file I/O (text, binary, encodings, modes), pathlib, tempfile, shutil.
All files are created inside a temporary directory that is removed at the end."""
import os
import shutil
import tempfile
from pathlib import Path

if __name__ == "__main__":
    with tempfile.TemporaryDirectory(prefix="ch09_") as tmp:
        base = Path(tmp)

        print("== writing and reading text ==")
        notes = base / "notes.txt"                       # '/' joins paths
        with open(notes, "w", encoding="utf-8") as f:    # 'w' creates/truncates
            f.write("first line\n")
            f.writelines(["second line\n", "café ☕\n"])
        with open(notes, "a", encoding="utf-8") as f:    # 'a' appends
            print("appended line", file=f)
        with open(notes, encoding="utf-8") as f:         # default mode 'r'
            for i, line in enumerate(f, 1):              # lazy, line by line
                print(f"  {i}: {line.rstrip()}")
        print("  read() whole file ->", len(notes.read_text(encoding="utf-8")), "characters")

        print("\n== text vs bytes, encodings ==")
        raw = notes.read_bytes()
        print("  bytes on disk:", len(raw), "(é is 2 bytes, ☕ is 3 bytes in UTF-8)")
        print("  first 12 bytes:", raw[:12])
        try:
            notes.read_text(encoding="ascii")
        except UnicodeDecodeError as exc:
            print("  ascii decode ->", type(exc).__name__, exc.reason, "at byte", exc.start)
        print("  latin-1 (wrong codec) mangles:", repr(notes.read_text(encoding="latin-1").splitlines()[2]))
        print("  errors='replace':", notes.read_text(encoding="ascii", errors="replace").splitlines()[2])

        print("\n== binary mode, seek/tell ==")
        blob = base / "data.bin"
        with open(blob, "wb") as f:
            f.write(bytes(range(10)))
        with open(blob, "rb") as f:
            f.seek(4)                                    # jump to byte 4
            print("  tell after seek(4):", f.tell(), "| read(3):", list(f.read(3)))

        print("\n== 'x' mode refuses to overwrite ==")
        try:
            open(notes, "x")
        except FileExistsError as exc:
            print("  FileExistsError:", exc.strerror)

        print("\n== pathlib ==")
        p = base / "reports" / "2026" / "q3.final.csv"
        print("  name:", p.name, "| stem:", p.stem, "| suffix:", p.suffix, "| suffixes:", p.suffixes)
        print("  parent relative to base:", p.parent.relative_to(base))
        print("  with_suffix('.json'):", p.with_suffix(".json").name)
        p.parent.mkdir(parents=True, exist_ok=True)
        p.write_text("id,total\n1,9.5\n", encoding="utf-8")
        (base / "reports" / "2026" / "q2.csv").write_text("id,total\n", encoding="utf-8")
        (base / "reports" / "readme.md").write_text("# reports\n", encoding="utf-8")
        print("  exists:", p.exists(), "| is_file:", p.is_file(), "| size:", p.stat().st_size, "bytes")
        print("  glob('*/*.csv'):   ", sorted(x.name for x in (base / "reports").glob("*/*.csv")))
        print("  rglob('*.md'):     ", [x.name for x in base.rglob("*.md")])
        print("  iterdir(reports):  ", sorted(x.name for x in (base / "reports").iterdir()))

        print("\n== shutil: copy, move, archive, delete trees ==")
        backup = base / "backup"
        shutil.copytree(base / "reports", backup)
        print("  copytree ->", sorted(str(x.relative_to(backup)) for x in backup.rglob("*")))
        shutil.move(backup / "readme.md", base / "README.md")
        print("  moved readme ->", (base / "README.md").exists())
        archive = shutil.make_archive(str(base / "reports_bak"), "zip", root_dir=backup)
        print("  archive:", Path(archive).name, Path(archive).stat().st_size > 0)
        shutil.rmtree(backup)
        print("  rmtree backup -> exists?", backup.exists())
        print("  disk_usage works:", shutil.disk_usage(base).total > 0)

        print("\n== tempfile ==")
        with tempfile.NamedTemporaryFile("w+", suffix=".txt", dir=base, delete=True,
                                         encoding="utf-8") as tf:
            tf.write("scratch")
            tf.seek(0)
            print("  temp file", Path(tf.name).suffix, "contains:", tf.read())
        print("  deleted after close:", not Path(tf.name).exists())

        print("\n== atomic replace pattern ==")
        target = base / "settings.json"
        target.write_text('{"v": 1}', encoding="utf-8")
        fd, tmp_name = tempfile.mkstemp(dir=base, suffix=".tmp")
        with os.fdopen(fd, "w", encoding="utf-8") as f:
            f.write('{"v": 2}')
        os.replace(tmp_name, target)                     # atomic on the same filesystem
        print("  settings now:", target.read_text(encoding="utf-8"))

    print("\ntemp dir removed:", not Path(tmp).exists())
