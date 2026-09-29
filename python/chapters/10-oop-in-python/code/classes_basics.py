"""Classes, instances, attributes, the three kinds of method, and properties."""


class BankAccount:
    bank_name = "PyBank"          # class attribute: shared by every instance
    _next_id = 1                  # class attribute used as a counter

    def __init__(self, owner: str, balance: float = 0.0) -> None:
        self.owner = owner        # instance attributes: one copy per object
        self._balance = balance   # leading underscore = "internal, please don't touch"
        self.id = BankAccount._next_id
        BankAccount._next_id += 1

    # --- instance method: receives the instance as `self`
    def deposit(self, amount: float) -> None:
        if amount <= 0:
            raise ValueError("amount must be positive")
        self._balance += amount

    # --- property: looks like an attribute, runs code
    @property
    def balance(self) -> float:
        return self._balance

    @balance.setter
    def balance(self, value: float) -> None:
        if value < 0:
            raise ValueError("balance cannot be negative")
        self._balance = value

    # --- class method: receives the class as `cls` (alternative constructor)
    @classmethod
    def from_csv(cls, line: str) -> "BankAccount":
        owner, balance = line.split(",")
        return cls(owner, float(balance))

    # --- static method: no self/cls, just lives in the class namespace
    @staticmethod
    def is_valid_amount(amount: float) -> bool:
        return amount > 0


class Buggy:
    items = []                    # MISTAKE: one list shared by all instances

    def add(self, x):
        self.items.append(x)


class Fixed:
    def __init__(self):
        self.items = []           # each instance gets its own list

    def add(self, x):
        self.items.append(x)


if __name__ == "__main__":
    a = BankAccount("Asha", 100)
    b = BankAccount.from_csv("Ben,50")
    a.deposit(25)
    print("a:", a.owner, a.balance, "id", a.id)
    print("b:", b.owner, b.balance, "id", b.id)
    print("static check:", BankAccount.is_valid_amount(-5))

    # class vs instance attribute lookup
    print("a.bank_name:", a.bank_name)
    a.bank_name = "Shadow"        # creates an INSTANCE attribute that shadows the class one
    print("after assignment -> a:", a.bank_name, "| b:", b.bank_name,
          "| class:", BankAccount.bank_name)
    print("a.__dict__:", a.__dict__)

    # property setter validation
    try:
        a.balance = -1
    except ValueError as e:
        print("setter refused:", e)

    # bound methods: a.deposit is the function with `self` pre-filled
    print("a.deposit is bound:", a.deposit.__self__ is a)
    BankAccount.deposit(a, 5)     # identical to a.deposit(5)
    print("after unbound call:", a.balance)

    # mutable class attribute pitfall
    x, y = Buggy(), Buggy()
    x.add(1)
    print("Buggy y.items:", y.items)
    p, q = Fixed(), Fixed()
    p.add(1)
    print("Fixed q.items:", q.items)
