"""argparse: positionals, options, types, choices, flags, subcommands."""
import argparse


def build_parser() -> argparse.ArgumentParser:
    p = argparse.ArgumentParser(prog="todo", description="A tiny todo CLI.")
    p.add_argument("-v", "--verbose", action="count", default=0,
                   help="more output (-vv for even more)")
    sub = p.add_subparsers(dest="command", required=True)

    add = sub.add_parser("add", help="add a task")
    add.add_argument("title", help="task title")
    add.add_argument("--priority", type=int, choices=range(1, 4), default=2)
    add.add_argument("--tag", action="append", default=[], help="repeatable")

    ls = sub.add_parser("list", help="list tasks")
    ls.add_argument("--done", action="store_true", help="only finished tasks")
    ls.add_argument("--limit", type=int, default=10)
    return p


def main(argv: list[str] | None = None) -> int:
    args = build_parser().parse_args(argv)     # None -> reads sys.argv[1:]
    print("  parsed:", vars(args))
    return 0


if __name__ == "__main__":
    parser = build_parser()
    for argv in (["add", "Buy milk", "--priority", "1", "--tag", "home", "--tag", "shop"],
                 ["-vv", "list", "--done"]):
        print("$ todo", " ".join(argv))
        main(argv)
    print("$ todo add x --priority 9")
    try:
        main(["add", "x", "--priority", "9"])
    except SystemExit as e:                    # argparse exits with code 2
        print("  exit code:", e.code)
    print(parser.format_usage().strip())
