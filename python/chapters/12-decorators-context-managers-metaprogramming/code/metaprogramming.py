"""type(), metaclasses, __init_subclass__, class registries."""


# ---------- classes are objects created by type
def speak(self):
    return f"{self.name} speaks"


Dog = type("Dog", (object,), {"name": "Rex", "speak": speak})   # same as a class statement


# ---------- a metaclass: the class of a class
class RegistryMeta(type):
    registry: dict[str, type] = {}

    def __new__(mcls, name, bases, namespace):
        cls = super().__new__(mcls, name, bases, namespace)
        if bases:                                   # skip the abstract root
            mcls.registry[name.lower()] = cls
        return cls


class Plugin(metaclass=RegistryMeta):
    pass


class CsvExporter(Plugin): ...
class JsonExporter(Plugin): ...


# ---------- the same thing with __init_subclass__ (simpler, preferred)
class Handler:
    handlers: dict[str, type] = {}

    def __init_subclass__(cls, /, route: str, **kwargs):
        super().__init_subclass__(**kwargs)
        Handler.handlers[route] = cls


class Home(Handler, route="/"): ...
class About(Handler, route="/about"): ...


# ---------- enforcing a rule at class-creation time
class RequiresRun:
    def __init_subclass__(cls, **kwargs):
        super().__init_subclass__(**kwargs)
        if not callable(cls.__dict__.get("run")):
            raise TypeError(f"{cls.__name__} must define run()")


class GoodJob(RequiresRun):
    def run(self):
        return "ran"


if __name__ == "__main__":
    print(Dog().speak(), "| type(Dog):", type(Dog).__name__, "| type(type):", type(type).__name__)
    print("type(42):", type(42).__name__, "| type(int):", type(int).__name__)
    print("metaclass registry:", RegistryMeta.registry)
    print("type(CsvExporter):", type(CsvExporter).__name__)
    print("__init_subclass__ routes:", {k: v.__name__ for k, v in Handler.handlers.items()})
    print("GoodJob:", GoodJob().run())
    try:
        class BadJob(RequiresRun):
            pass
    except TypeError as e:
        print("TypeError:", e)
