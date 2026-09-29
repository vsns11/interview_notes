"""Lists: create, access, update, join, slice, loop, operators, add/remove, methods."""


def create_access_update() -> None:
    my_list = [1, "Hello", 3.14, True]           # mixed types are fine
    print(my_list, len(my_list))
    print(my_list[0], my_list[1], my_list[-1], my_list[-2])
    my_list[1] = "World"
    print("single update:", my_list)
    my_list[1:3] = ["Changed", "Completely"]
    print("slice update:", my_list)
    print(list("abc"), list(range(4)), [0] * 3, [])


def join_slice_loop() -> None:
    a, b = [1, 2, 3], [4, 5, 6]
    c = a + b                                    # new list
    a.extend(b)                                  # modifies a in place
    print(c, a, c == a, c is a)
    full = list(range(10))
    print(full[2:7], full[-8:-3], full[::3], full[::-1])
    for i, fruit in enumerate(["apple", "banana", "cherry"], start=1):
        print(i, fruit)


def operators() -> None:
    a, b = [1, 2, 3], [4, 5, 6]
    print(a + b, a * 3, 3 in a, 7 not in a)
    print([1, 2, 3] < [1, 3], [1, 2] == [1, 2], max(a), min(a), sum(a))


def add_remove() -> None:
    fruits = ["apple", "banana"]
    fruits.append("cherry");               print("append:", fruits)
    fruits.insert(1, "orange");            print("insert:", fruits)
    fruits.extend(["grape", "mango"]);     print("extend:", fruits)
    fruits.remove("banana");               print("remove:", fruits)
    last = fruits.pop();                   print("pop():", fruits, "->", last)
    first = fruits.pop(0);                 print("pop(0):", fruits, "->", first)
    del fruits[0];                         print("del [0]:", fruits)
    fruits.clear();                        print("clear:", fruits)
    x = [1, 2]
    x.append([3, 4]);  print("append a list :", x)   # nests!
    y = [1, 2]
    y.extend([3, 4]);  print("extend a list :", y)
    try:
        y.remove(99)
    except ValueError as e:
        print("ValueError:", e)


def methods() -> None:
    colors = ["red", "blue", "green", "blue"]
    print(colors.count("blue"), colors.index("blue"), colors.index("blue", 2))
    colors.remove("blue");   print(colors)          # only the first 'blue'
    colors.append("amber")
    colors.sort();           print("sort:", colors)
    colors.reverse();        print("reverse:", colors)
    nums = [3, 1, 2]
    snapshot = nums.copy()
    nums.sort(reverse=True)
    print(nums, snapshot)


def slice_assignment() -> None:
    x = list(range(10))
    print(x[1:8:2], x[::-2], x[-3:])
    y = x.copy(); y[2:5] = ["a"];            print("shrink :", y)      # lengths may differ
    y = x.copy(); y[2:2] = ["new", "items"]; print("insert :", y)      # empty slice = insert
    y = x.copy(); y[::2] = [0] * 5;          print("stepped:", y)      # lengths must match
    y = x.copy(); del y[::2];                print("del ::2:", y)
    y = x.copy(); y[:] = [];                 print("clear  :", y)
    try:
        y = x.copy(); y[::2] = [1, 2]
    except ValueError as e:
        print("ValueError:", e)


if __name__ == "__main__":
    for f in (create_access_update, join_slice_loop, operators, add_remove, methods, slice_assignment):
        print(f"--- {f.__name__}")
        f()
