"""Chapter 7 - LEGB scope, global/nonlocal, closures."""
import builtins

level = "global"                     # G: module-level name


def outer():
    level = "enclosing"              # E: for inner()

    def inner():
        level = "local"              # L
        print("  inner sees:", level)

    inner()
    print("  outer sees:", level)


def reads_global():
    print("  reads_global sees:", level)   # no assignment here -> global


counter = 0


def broken_increment():
    counter += 1                     # assignment makes counter LOCAL -> error


def global_increment():
    global counter
    counter += 1


def make_counter():
    count = 0                        # lives in the enclosing scope

    def step():
        nonlocal count               # rebind the enclosing variable
        count += 1
        return count

    return step


def make_multiplier(factor):
    def multiply(x):
        return x * factor            # 'factor' is captured (a free variable)
    return multiply


if __name__ == "__main__":
    print("== LEGB lookup ==")
    outer()
    reads_global()
    print("  B (built-in) len is builtins.len:", len is builtins.len)

    print("\n== assigning without global ==")
    try:
        broken_increment()
    except UnboundLocalError as exc:
        print("  UnboundLocalError:", exc)

    print("\n== global ==")
    global_increment(); global_increment()
    print("  counter =", counter)

    print("\n== nonlocal + closures ==")
    c1, c2 = make_counter(), make_counter()
    print("  c1:", c1(), c1(), c1(), " c2:", c2())
    double, triple = make_multiplier(2), make_multiplier(3)
    print("  double(5) =", double(5), " triple(5) =", triple(5))
    print("  free vars of double:", double.__code__.co_freevars,
          "cell value:", double.__closure__[0].cell_contents)

    print("\n== late-binding trap ==")
    funcs = [lambda: i for i in range(3)]
    print("  lambdas in a loop:", [f() for f in funcs])
    funcs = [lambda i=i: i for i in range(3)]
    print("  with i=i default:  ", [f() for f in funcs])

    print("\n== shadowing a built-in ==")
    list_ = list("abc")
    print("  fine:", list_)
    ns = {}
    exec("list = [1, 2]\ntry:\n    list('xy')\nexcept TypeError as e:\n    err = str(e)", ns)
    print("  after 'list = [1, 2]': list('xy') ->", ns["err"])
