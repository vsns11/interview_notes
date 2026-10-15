# Chapter 2 — Basics: Types, Variables and Operators

Programs are built from **variables** holding **values** of a **type** (the kind of data — whole number, decimal, text, true/false — which decides what you can do with it), **operators** that combine them, and **statements** executed in order. This chapter covers them precisely, including Java's silent surprises: integer overflow (a too-big number wrapping round), integer division (drops the remainder), floating-point rounding (decimals stored approximately) and `==` on strings. It ends with text blocks, `String` basics and console I/O.

Code: [`code/`](code/), compiled with `javac --release 25`, output from real runs. Exam traps: OCP [Chapter 1 — Building Blocks](../../../../ocp-java21-study/chapters/01-building-blocks/README.md) (types, literals, `var`, text blocks, scope) and [Chapter 2 — Operators](../../../../ocp-java21-study/chapters/02-operators/README.md) (promotion, casting, precedence).

---

## 2.1 Statements, identifiers, keywords and comments

### What it is
- A **statement** is one complete instruction ending in `;` (`int x = 5;`). A **block** `{ … }` groups statements and can appear wherever a statement can.
- An **expression** produces a value (`x + 1`, `name.length()`, `a > b`); an *expression statement* uses one as a statement (`x++;`).
- An **identifier** is a name you choose for a variable, method (named, callable block of code), class (blueprint for a kind of object) or package (named group of classes, like a folder).

### Identifier rules
| Rule | Legal | Illegal |
|---|---|---|
| Starts with a letter, `$` or `_` | `total`, `$price`, `_temp` | `2fast` |
| Then letters, digits, `$`, `_` | `item2`, `max_value` | `my-name`, `a b` |
| Not a reserved keyword or `true`/`false`/`null` | `klass`, `clazz` | `class`, `null` |
| A single `_` is a keyword (*unnamed variables*, [Chapter 3](../03-control-flow/README.md)) | `__` | `_` |
| Case-sensitive | `count` ≠ `Count` | — |

Unicode letters are allowed (`größe`); `$` is legal but by convention reserved for generated code.

### Keywords
Java 25 has **51 reserved keywords** that can never be identifiers — e.g. `class`, `int`, `if`, `for`, `new`, `static`, `final`, `return`, `try`, plus `const` and `goto` (reserved, unused), `strictfp` (obsolete) and `_`. It has **17 contextual keywords**, keywords only in certain positions so old code using them as names still compiles: `var`, `yield`, `record`, `sealed`, `non-sealed`, `permits`, `when`, and the module words `module`, `open`, `opens`, `exports`, `requires`, `transitive`, `uses`, `provides`, `to`, `with`. `true`, `false` and `null` are *literals* (values written directly in code), not keywords, but are still unusable as names.

### Comments
```java
// line comment: to the end of the line
/* block comment:
   can span lines (cannot be nested) */
/** Javadoc comment: documents the next class, method or field. @param, @return tags… */
```
Comment **why**, not what the code already says.

---

## 2.2 Variables

### What it is
A **variable** is a named storage location with a fixed **type**. You **declare** it (`type name;`), **initialise** it (`name = value;`, often together: `int age = 30;`), then read or reassign it with values of that type. It exists within its **scope** — from the declaration to the closing brace of its block.

[`code/Variables.java`](code/Variables.java):
```java
static int counter;          // field: gets a default value (0) automatically

public static void main(String[] args) {
    int age;                 // declaration
    age = 30;                // initialisation (assignment)
    int year = 2025, month = 9;   // declare + initialise several of the same type
    age = age + 1;           // reassignment
    final double TAX_RATE = 0.18; // final: can be assigned exactly once
    // TAX_RATE = 0.2;       // compile error: cannot assign a value to final variable TAX_RATE

    System.out.println("age=" + age + " year=" + year + " month=" + month);
    System.out.println("TAX_RATE=" + TAX_RATE + " counter (field default)=" + counter);

    {                                   // an inner block
        int inner = 5;                  // exists only inside these braces
        System.out.println("inner=" + inner + ", age visible here too: " + age);
    }
    // System.out.println(inner);      // compile error: cannot find symbol

    int count;
    if (age > 18) count = 1; else count = 0;   // definitely assigned on every path
    System.out.println("count=" + count);
}
```
Output:
```text
age=31 year=2025 month=9
TAX_RATE=0.18 counter (field default)=0
inner=5, age visible here too: 31
count=1
```

### Local variables vs fields
| | Local variable (in a method/block) | Field (in a class) |
|---|---|---|
| Default value | **None** — must be assigned before use, else *"variable x might not have been initialized"* | `0`, `0.0`, `false`, `'\u0000'`, or `null` for references |
| Lifetime | Until the block ends | As long as the object (instance field — one per object) or class (`static` field — one shared copy) |
| Modifiers | Only `final` | Access modifiers (`public`, `private`… — who may use it), `static`, `final`, … ([Chapter 5](../05-oop-classes-and-objects/README.md)) |

**Definite assignment** analysis checks that every path assigns a local before it is read — so `count` above compiles, while `int n; System.out.println(n);` does not.

### `final`
`final` = *assigned once*. A `final` primitive never changes; a `final` reference cannot point elsewhere but the **object can still change** (`final List<String> xs = new ArrayList<>(); xs.add("ok");` is legal — lists: [Chapter 10](../10-collections/README.md)). Use it for constants (`static final int MAX_RETRIES = 3;`) and to clarify intent.

### Common mistakes
- Reading a local before assigning it on every branch.
- Redeclaring a name in scope (`int x = 1; { int x = 2; }` — locals cannot shadow, i.e. hide, other locals).
- Thinking `final` makes an object immutable (unchangeable after creation).

---

## 2.3 Primitive types

### What it is
Java has exactly **eight primitive types** — simple values stored directly, not as objects; everything else is a reference type. Sizes and ranges are fixed on every platform, unlike C. Sizes are in **bits** (binary digits); 8 bits = one **byte**.

[`code/Primitives.java`](code/Primitives.java) prints the real values from the wrapper classes ([2.5](#25-reference-types-and-null)):
```java
System.out.printf("%-8s %5d  %-22d %d%n", "int", Integer.SIZE, Integer.MIN_VALUE, Integer.MAX_VALUE);
// ... one line per type
```
Output:
```text
type      bits  min                    max
byte         8  -128                   127
short       16  -32768                 32767
int         32  -2147483648            2147483647
long        64  -9223372036854775808   9223372036854775807
char        16  0                      65535
float       32  1.4E-45                3.4028235E38
double      64  4.9E-324               1.7976931348623157E308
boolean  true or false (size not specified by the language)
```

| Type | Kind | Size | Range / precision | Default (fields) | Use it for |
|---|---|---|---|---|---|
| `byte` | signed integer | 8 bits | −128 … 127 | `0` | raw binary data, I/O buffers (memory holding data being read/written) |
| `short` | signed integer | 16 bits | −32 768 … 32 767 | `0` | rarely — large arrays of small numbers |
| `int` | signed integer | 32 bits | about ±2.1 billion (−2³¹ … 2³¹−1 = 2 147 483 647) | `0` | **the default integer** — counts, indexes |
| `long` | signed integer | 64 bits | about ±9.2 × 10¹⁸ | `0L` | epoch milliseconds (ms since 1 Jan 1970 UTC), IDs, cents, big counters |
| `float` | IEEE 754 binary floating point | 32 bits | ~6–7 significant digits, up to ±3.4 × 10³⁸ | `0.0f` | graphics, large arrays where memory matters |
| `double` | IEEE 754 binary floating point | 64 bits | ~15–16 significant digits, up to ±1.8 × 10³⁰⁸ | `0.0` | **the default decimal** — measurements, science |
| `char` | unsigned UTF-16 code unit | 16 bits | `'\u0000'` … `'￿'` (0 … 65 535) | `'\u0000'` | single characters |
| `boolean` | logical | not specified | `true`, `false` | `false` | conditions, flags |

- *Signed* types hold negatives; *unsigned* only zero and positives. **IEEE 754** is the standard for storing floating-point numbers; **UTF-16** stores characters as 16-bit units.
- `float`/`double` `MIN_VALUE` is the smallest **positive** (subnormal — below the normal range, with reduced precision) value; the most negative is `-Double.MAX_VALUE`.
- Characters outside the Basic Multilingual Plane (the first 65 536 Unicode characters; many emoji lie beyond) need two `char`s (a *surrogate pair*); see [Chapter 7](../07-core-apis/README.md).
- `boolean` is not a number: `if (1)` does not compile, and you cannot cast between `boolean` and `int`.
- Beyond the table: money and exact decimals → `long` cents or `BigDecimal` (exact decimal class), never `double` ([2.11](#211-floating-point-numbers)); arbitrary-size integers → `BigInteger` ([Chapter 7](../07-core-apis/README.md)); file sizes → `long`.

---

## 2.4 Literals

### What it is
A **literal** is a value written directly in source; its form decides its type. Integer bases: **hexadecimal** (base 16, `0x`), **octal** (base 8, `0`), **binary** (base 2, `0b`).

[`code/Literals.java`](code/Literals.java):
```java
int decimal = 255, hex = 0xFF, octal = 0377, binary = 0b1111_1111;
long big = 9_000_000_000L;              // L suffix: needed beyond int range
float price = 19.99f;                   // f suffix: literal is double otherwise
double sci = 6.022e23, plain = 3.14;
char letter = 'A', unicode = 'é', tab = '\t';
boolean ready = true;
String text = "He said \"hi\"\\n";      // escaped quote and backslash

System.out.println(decimal + " " + hex + " " + octal + " " + binary);
System.out.println(big + " " + price + " " + sci + " " + plain);
System.out.println(letter + " " + unicode + " [" + tab + "] " + ready);
System.out.println(text);
System.out.println("char 'A' as a number: " + (int) letter + ", 'A' + 1 = " + (letter + 1)
        + ", (char) ('A' + 1) = " + (char) (letter + 1));
```
Output:
```text
255 255 255 255
9000000000 19.99 6.022E23 3.14
A é [	] true
He said "hi"\n
char 'A' as a number: 65, 'A' + 1 = 66, (char) ('A' + 1) = B
Integer.toBinaryString(10) = 1010, toHexString(255) = ff
```

| Literal | Form | Type |
|---|---|---|
| Decimal integer | `42`, `1_000_000` | `int` |
| Long | `42L` (capital `L`; `l` looks like `1`) | `long` |
| Hexadecimal | `0xFF`, `0xCAFE_BABE` | `int` (or `long` with `L`) |
| Octal | leading `0`: `0377` = 255 | `int` — beware: `010` is **8** |
| Binary | `0b1010` (Java 7) | `int` |
| Floating point | `3.14`, `1e-9`, `.5`, `2.` | `double` |
| Float | `3.14f` | `float` |
| Character | `'A'`, `'\n'`, `'é'` | `char` |
| Boolean | `true`, `false` | `boolean` |
| String | `"text"`, text blocks `"""…"""` | `String` |
| Null | `null` | the null type (assignable to any reference) |

**Underscores** (Java 7) separate digits (`1_000_000`, `0b1111_0000`) but not at the start or end of the digits, next to the decimal point, next to an `L`/`f` suffix, or right after `0x`/`0b`.

**Escape sequences**: `\n` newline, `\t` tab, `\r` carriage return, `\b` backspace, `\f` form feed, `\"`, `\'`, `\\`, `\s` space (Java 15), octal `\0`–`\377`, Unicode `\uXXXX`.

### Common mistakes
- `long big = 9000000000;` → *"integer number too large"*: the literal is an `int` until you add `L`.
- `float f = 1.5;` → *possible lossy conversion from double to float*; write `1.5f`.
- Leading zeros for alignment: `int code = 010;` is 8.

---

## 2.5 Reference types and `null`

### What it is
Every non-primitive type is a **reference type**: classes (`String`, `Scanner`), interfaces (`List`), arrays (`int[]`), enums and records (Chapters 4–6). A reference variable holds a **reference** — a handle to an object on the heap ([1.8](../01-java-and-the-jvm/README.md#18-memory-and-garbage-collection--an-overview)) — or **`null`**, "points to nothing".

```mermaid
flowchart LR
    subgraph Stack["Stack frame of main"]
        A["int count = 3"]
        B["String name"]
        C["String other"]
        D["String missing = null"]
    end
    subgraph Heap
        S["String object: Ada"]
    end
    B --> S
    C --> S
```
- `other = name` copies the **arrow**, not the object: both point to the same `String`.
- Calling a method through `null` throws **`NullPointerException`**. *Throwing* an **exception** signals an error that stops normal execution unless caught ([Chapter 8](../08-exceptions/README.md)). Since Java 14 (JEP 358, default from 15) *helpful NullPointerException* messages name what was null: *Cannot invoke "String.length()" because "name" is null*.

| | Primitive | Reference |
|---|---|---|
| Holds | the value itself | a pointer to an object, or `null` |
| Default (field) | `0`/`false`/`'\u0000'` | `null` |
| `==` compares | values | **identity** (same object?) |
| Has methods | no | yes (`name.length()`) |

Every primitive has a **wrapper class** (`Integer`, `Double`, `Character`, `Boolean`…) for use where an object is required, e.g. `List<Integer>` (`<…>` is a *generic* type argument, [Chapter 9](../09-generics/README.md)); Java converts automatically (*autoboxing/unboxing*). Details and the `Integer` cache trap: [Chapter 7](../07-core-apis/README.md).

---

## 2.6 `var` — local variable type inference (Java 10)

### What it is
`var` (Java 10, JEP 286) lets the compiler **infer** a local variable's type from its initialiser (the `= value` part). The variable is still statically typed — fixed at compile time, just not written.

[`code/VarDemo.java`](code/VarDemo.java):
```java
var count = 10;                          // int
var ratio = 10 / 4.0;                    // double
var name = "Ada";                        // String
var scores = new HashMap<String, List<Integer>>();   // HashMap<String, List<Integer>>
scores.put(name, new ArrayList<>(List.of(90, 85)));
for (var entry : scores.entrySet()) {    // Map.Entry<String, List<Integer>>
    System.out.println(entry.getKey() + " -> " + entry.getValue());
}
// count = "ten";                        // compile error: the type is fixed at int
var items = new ArrayList<>();           // careful: ArrayList<Object>
items.add(1); items.add("two");
System.out.println(count + " " + ratio + " " + items);
```
Output:
```text
Ada -> [90, 85]
10 2.5 [1, two]
{a=1}
```

### Where `var` is allowed
| Allowed | Not allowed |
|---|---|
| Local variables with an initialiser | Fields, method parameters, return types |
| `for` and for-each loop variables | No initialiser: `var x;` |
| try-with-resources variables ([Chapter 8](../08-exceptions/README.md)) | `var x = null;` (no type to infer) |
| Lambda parameters (Java 11): `(var a, var b) -> a + b` — lets you add annotations (`@`-tags like `@Override`) | Array initialiser alone: `var a = {1, 2};` |
| | Multiple variables: `var a = 1, b = 2;` |

`var` is a contextual keyword: `var var = 1;` compiles, but no *type* may be named `var`.

### Trade-offs and style
Use `var` when the type is obvious from the right side (`new`, a well-named factory method) or long and noisy (`Map<String, List<Integer>>`). Avoid it when the reader cannot see the type (`var result = service.process();`) or the inferred type is not what you want (`new ArrayList<>()` → `ArrayList<Object>`; `var x = 1;` is `int`, not `long`). OpenJDK's *Local Variable Type Inference Style Guidelines* agree: clarity for the reader.

---

## 2.7 Type conversion: widening, narrowing, casting, promotion

### What it is
Java does **safe** conversions automatically and needs an explicit **cast** — `(type) value` — for risky ones.

```mermaid
flowchart LR
    byte --> short --> int --> long --> float --> double
    char --> int
```
Arrows are **widening primitive conversions**, automatic (`int i = 'A'; long l = i; double d = l;`). Going against them is **narrowing** and needs a cast.

### How it works
1. **Widening** is automatic but not always lossless: `int → float`, `long → float` and `long → double` keep magnitude but can lose **precision** — `float` has a 24-bit mantissa (the part storing significant digits), `double` 53 bits vs `long`'s 64.
2. **Narrowing** (cast): `(int) 3.99` truncates toward zero to `3` (`(int) -3.99` is `-3`); `(byte) 300` keeps the low 8 bits (44); `(byte) 200` is −56 (bits `11001000` read as signed). Floating → integer clamps out-of-range values to MIN/MAX and turns NaN ("Not a Number", e.g. 0.0/0) into 0.
3. **Numeric promotion**: before a binary arithmetic operator runs, `byte`, `short` and `char` operands become `int`; if either operand is `long`, `float` or `double`, both widen to the wider type. So `byte + byte` is `int`, and `'a' + 1` is the `int` 98.
4. **Compile-time constants** (literals, or `final` variables set from literals) may be assigned to a narrower type if they fit: `byte b = 10 + 20;` is fine; `byte b = x + y;` is not.
5. **Compound assignment** (`+=`, `-=`, `*=`…) hides a cast: `x += 5` means `x = (T) (x + 5)`.

[`code/Conversions.java`](code/Conversions.java) (key lines):
```java
double pi = 3.99;
int truncated = (int) pi;          // narrowing needs a cast; drops the fraction
long rounded = Math.round(pi);     // rounding is a method, not a cast

byte b = (byte) 300;               // keeps only the low 8 bits: 300 - 256 = 44
int precise = 123_456_789;
float f = precise;                 // widening but LOSSY: float has 24 bits of precision

byte x = 10, y = 20;
// byte sum = x + y;               // compile error: x + y is an int
byte sum = (byte) (x + y);
x += 5;                            // compound assignment hides a cast: x = (byte) (x + 5)

long wrong = 1_000_000 * 1_000_000;       // int overflow BEFORE widening to long
long right = 1_000_000L * 1_000_000;
```
Output:
```text
widened: 100 100.0
(int) 3.99 = 3, Math.round(3.99) = 4
(byte) 300 = 44, (byte) 200 = -56
(int) 1e20 = 2147483647, (int) Double.NaN = 0
int 123456789 -> float -> int: 123456792
sum=30 sum2=30 x=15
constant=30 'a'+1=98 7/2=3 7/2.0=3.5
wrong=-727379968 right=1000000000000
```
The last line is the classic bug: the multiplication runs in `int`, overflows, and only then widens. Put `L` on the **first** operand (`24L * 60 * 60 * 1000 * 365`).

### Common mistakes
- Expecting `(int)` to round — use `Math.round`, `Math.floor`, `Math.ceil`.
- `7 / 2` is `3` (integer division). Write `7 / 2.0` or `(double) 7 / 2`; `(double) (7 / 2)` is `3.0` — cast too late.
- Assuming widening is lossless.

### In the interview
**Q: Why doesn't `short s = 1; s = s + 1;` compile, but `s += 1;` does?** `s + 1` is promoted to `int`, and assigning `int` to `short` needs a cast; compound assignment includes it implicitly.
- *`(byte) 200`?* −56.
- *Is `long → double` safe?* Magnitude yes, precision no (53-bit vs 64-bit).
- *`(int) 7.9 + (int) -7.9`?* 7 + (−7) = 0.

---

## 2.8 Operators

### What it is
An **operator** computes a value from one (*unary*), two (*binary*) or three (*ternary*) operands.

### Arithmetic: `+ - * / %` and `++ --`
[`code/Arithmetic.java`](code/Arithmetic.java):
```java
System.out.println("17 / 5 = " + 17 / 5 + ", 17 % 5 = " + 17 % 5);
System.out.println("-17 / 5 = " + -17 / 5 + ", -17 % 5 = " + -17 % 5
        + ", Math.floorMod(-17, 5) = " + Math.floorMod(-17, 5));
System.out.println("17.0 / 5 = " + 17.0 / 5 + ", 5.5 % 2 = " + 5.5 % 2);
System.out.println("1.0 / 0 = " + 1.0 / 0 + ", 0.0 / 0 = " + 0.0 / 0);
try { System.out.println(1 / 0); }
catch (ArithmeticException e) { System.out.println("1 / 0 -> ArithmeticException: " + e.getMessage()); }

int a = 5;
int b = a++;     // post-increment: b gets the old value, then a becomes 6
int c = ++a;     // pre-increment: a becomes 7, then c gets 7
int n = 3;
int result = n++ + ++n;   // 3 + 5
```
Output:
```text
17 / 5 = 3, 17 % 5 = 2
-17 / 5 = -3, -17 % 5 = -2, Math.floorMod(-17, 5) = 3
17.0 / 5 = 3.4, 5.5 % 2 = 1.5
1.0 / 0 = Infinity, 0.0 / 0 = NaN
1 / 0 -> ArithmeticException: / by zero
a=7 b=5 c=7
n++ + ++n with n=3 -> 8, n=5
compound ops -> x=1
-(-5) = 5, +3 = 3
```

| Rule | Detail |
|---|---|
| Integer `/` truncates toward zero | `-17 / 5` is `-3`; `-7 / 2` is `-3` |
| `%` takes the sign of the **dividend** | `-17 % 5` is `-2`, `-7 % 2` is `-1`. For a non-negative result (circular buffers, day of week) use `Math.floorMod` |
| Integer division by zero | throws `ArithmeticException` |
| Floating division by zero | no exception: `Infinity`, `-Infinity` or `NaN` |
| `x++` vs `++x` | both add 1; the expression's value is the old (postfix) or new (prefix) value — `int x = 5; int y = x++ * 2;` gives `y = 10`, `x = 6` |
| `+` with a `String` operand | concatenation ([2.12](#212-strings-and-text-blocks)) |

Use `++`/`--` as standalone statements; `n++ + ++n` is deterministic (operands evaluate left to right) but unreadable.

### Assignment operators
`=` and compound `+= -= *= /= %= &= |= ^= <<= >>= >>>=`. Assignment is an expression whose value is the assigned value, right-associative: `a = b = c = 7` sets all three.

### Relational and equality: `< <= > >= == !=`, `instanceof`
All return `boolean`. For **primitives** `==` compares values; for **references** it compares **identity** (same object). Compare contents with `equals`.

[`code/Comparison.java`](code/Comparison.java):
```java
String s1 = "java";
String s2 = new String("java");
System.out.println("s1 == s2: " + (s1 == s2) + ", s1.equals(s2): " + s1.equals(s2));
```
Output:
```text
s1 == s2: false, s1.equals(s2): true
```
`x instanceof Type` tests the object's type (false for `null`); the pattern form `x instanceof String s` is in [Chapter 3](../03-control-flow/README.md#35-pattern-matching-for-instanceof-java-16).

### Logical: `&& || !` and `& | ^`
| Operator | Name | Evaluates right side? |
|---|---|---|
| `&&` | conditional AND | only if left is `true` (**short-circuit**) |
| `\|\|` | conditional OR | only if left is `false` (short-circuit) |
| `!` | NOT | — |
| `&`, `\|` | logical AND / OR on booleans | **always** both sides |
| `^` | XOR (exactly one true) | always both |

```java
static boolean check(String label, boolean value) {
    System.out.println("  evaluated " + label);
    return value;
}
boolean r1 = check("left", false) && check("right", true);
boolean r2 = check("left", false) & check("right", true);

String name = null;
if (name != null && name.length() > 3) System.out.println("long name");   // safe: && stops at null
```
Output:
```text
&& short-circuits:
  evaluated left
& evaluates both:
  evaluated left
  evaluated right
r1=false r2=false true ^ true=false !true=false
null-safe check with && worked
```
Short-circuiting makes the *null guard* `x != null && x.foo()` safe; with `&` it throws `NullPointerException`.

### Ternary `?:`
`condition ? valueIfTrue : valueIfFalse` is an **expression**, unlike `if`:
```java
String category = age < 13 ? "child" : age < 20 ? "teen" : "adult";   // "adult" for 20
```
Keep to one level; otherwise use `if` or a `switch` expression ([Chapter 3](../03-control-flow/README.md)).

### Bitwise and shift: `& | ^ ~ << >> >>>`
These work on individual bits of integers. A *flag* is a yes/no setting in one bit; a *mask* is a value used with `&` to pick out bits. [`code/Bitwise.java`](code/Bitwise.java):
```java
static final int READ = 1, WRITE = 2, EXEC = 4;   // 001, 010, 100
int a = 0b1100, b = 0b1010;
System.out.println("a & b = " + bin(a & b) + ", a | b = " + bin(a | b)
        + ", a ^ b = " + bin(a ^ b) + ", ~a = " + (~a));
System.out.println("1 << 4 = " + (1 << 4) + ", 64 >> 2 = " + (64 >> 2));
System.out.println("-16 >> 2 = " + (-16 >> 2) + ", -16 >>> 28 = " + (-16 >>> 28));
int perms = READ | WRITE;                 // set two flags
perms &= ~WRITE;                          // clear a flag
```
Output:
```text
a & b = 1000, a | b = 1110, a ^ b = 110, ~a = -13
1 << 4 = 16, 64 >> 2 = 16
-16 >> 2 = -4, -16 >>> 28 = 15
1 << 32 = 1  (shift distance is taken mod 32 for int)
can write? true, can exec? false
after clearing WRITE: 1
is 12 even? true
```
| Operator | Meaning | Example |
|---|---|---|
| `&` | 1 where both bits are 1 | test a flag: `(perms & WRITE) != 0` |
| `\|` | 1 where either is 1 | set a flag |
| `^` | 1 where bits differ | toggle; `a ^ a == 0` |
| `~` | flip every bit; `~x == -x - 1` | clear with `& ~FLAG` |
| `<<` | shift left, fill with 0; `x << n` = x·2ⁿ (until overflow) | `1 << k` = k-th bit |
| `>>` | *arithmetic* shift right, copies the sign bit (top bit; 1 = negative) | `-16 >> 2 = -4` |
| `>>>` | *logical* shift right, fills with 0 | unsigned view of negatives |

The shift distance uses only the low 5 bits for `int` (6 for `long`), so `1 << 32 == 1`. Uses: permission flags, hash functions (`HashMap` mixes `h ^ (h >>> 16)`), `EnumSet`'s bit vector, network protocols, interview bit tricks ([Chapter 19](../19-java-for-coding-interviews/README.md)).

---

## 2.9 Operator precedence and associativity

### What it is
**Precedence** decides which operator binds first (`*` before `+`); **associativity** orders equal-precedence operators (mostly left-to-right; assignment and ternary right-to-left). Operands are always **evaluated left to right**.

| Level (high → low) | Operators | Associativity |
|---|---|---|
| 1 Postfix | `expr++` `expr--` | left |
| 2 Unary | `++expr` `--expr` `+` `-` `~` `!` `(type)` cast | right |
| 3 Multiplicative | `*` `/` `%` | left |
| 4 Additive | `+` `-` (and string `+`) | left |
| 5 Shift | `<<` `>>` `>>>` | left |
| 6 Relational | `<` `>` `<=` `>=` `instanceof` | left |
| 7 Equality | `==` `!=` | left |
| 8 Bitwise AND | `&` | left |
| 9 Bitwise XOR | `^` | left |
| 10 Bitwise OR | `\|` | left |
| 11 Conditional AND | `&&` | left |
| 12 Conditional OR | `\|\|` | left |
| 13 Ternary | `? :` | right |
| 14 Assignment, lambda | `=` `+=` `-=` … `->` | right |

[`code/Precedence.java`](code/Precedence.java):
```java
System.out.println("2 + 3 * 4 = " + (2 + 3 * 4));
System.out.println("(2 + 3) * 4 = " + ((2 + 3) * 4));
System.out.println("10 - 4 - 3 = " + (10 - 4 - 3) + "  (left to right)");
int a, b, c;
a = b = c = 7;                                  // assignment is right-associative
System.out.println("a=" + a + " b=" + b + " c=" + c);
System.out.println(1 + 2 + "3" + 4 + 5);        // left to right: 3, then "33", "334", "3345"
System.out.println("true || false && false = " + (true || false && false));
int x = 6;
System.out.println("x & 1 == 0 needs parentheses: " + ((x & 1) == 0));
```
Output:
```text
2 + 3 * 4 = 14
(2 + 3) * 4 = 20
10 - 4 - 3 = 3  (left to right)
a=7 b=7 c=7
3345
true || false && false = true
5 > 3 == true: true
x & 1 == 0 needs parentheses: true
```
- `&&` binds tighter than `||`: `true || (false && false)`.
- `==` binds tighter than `&`: `x & 1 == 0` means `x & (1 == 0)` — a compile error for `int x`. Parenthesise bitwise tests.
- `+` is left-associative: `1 + 2 + "3"` adds first, then concatenates.

**Rule of thumb:** unary → multiplicative → additive → comparison → equality → logical → ternary → assignment; parenthesise whenever a reader might hesitate.

---

## 2.10 Integer overflow

### What it is
Integer arithmetic is modulo 2³² (`int`) or 2⁶⁴ (`long`): a result outside the range silently **wraps around** — no exception, no warning. This is **overflow**.

> **Key term — two's complement:** how computers store negative integers. The top bit is the sign bit; −n is stored as the bit pattern of 2³² − n (for `int`). One adding circuit handles both signs, and adding 1 to the largest `int` yields the most negative one.

### Why it matters
The binary-search (repeatedly halving a sorted range) midpoint bug `(low + high) / 2` sat in the JDK's own `Arrays.binarySearch` for years (fixed 2006); timestamps multiplied in `int` give negative durations.

[`code/Overflow.java`](code/Overflow.java):
```java
int max = Integer.MAX_VALUE;
System.out.println("MAX_VALUE + 1 = " + (max + 1));
System.out.println("Math.abs(Integer.MIN_VALUE) = " + Math.abs(Integer.MIN_VALUE));

int low = 2_000_000_000, high = 2_100_000_000;
System.out.println("(low + high) / 2 = " + (low + high) / 2 + "   <- overflow bug");
System.out.println("low + (high - low) / 2 = " + (low + (high - low) / 2));
System.out.println("(low + high) >>> 1 = " + ((low + high) >>> 1));

try { Math.addExact(max, 1); }
catch (ArithmeticException e) { System.out.println("Math.addExact(MAX, 1) -> " + e.getMessage()); }
long seconds = 30L * 24 * 60 * 60 * 1000;     // L on the first operand: all long math
```
Output:
```text
MAX_VALUE + 1 = -2147483648
Math.abs(Integer.MIN_VALUE) = -2147483648
-Integer.MIN_VALUE = -2147483648
(low + high) / 2 = -97483648   <- overflow bug
low + (high - low) / 2 = 2050000000
(low + high) >>> 1 = 2050000000
Math.addExact(MAX, 1) -> integer overflow
Math.toIntExact(3e9) -> integer overflow
30 days in ms (long) = 2592000000, as int math = -1702967296
```
`Math.abs(Integer.MIN_VALUE)` stays negative because +2 147 483 648 does not fit in an `int`.

### How to defend
| Technique | Example |
|---|---|
| Use a wider type early | `long total = (long) a * b;` or `30L * …` |
| Throw instead of wrapping | `Math.addExact`, `subtractExact`, `multiplyExact`, `incrementExact`, `negateExact`, `toIntExact` (Java 8), `absExact` (Java 15) |
| Rearrange the formula | `low + (high - low) / 2` |
| Unlimited size | `BigInteger` ([Chapter 7](../07-core-apis/README.md)) |

### In the interview
**Q: What happens when an `int` overflows?** It wraps silently via two's complement (`MAX_VALUE + 1 == MIN_VALUE`). Use `long`, `Math.*Exact` (throws `ArithmeticException`), or `BigInteger`.
- *Where in practice?* Binary-search midpoints, hash-code arithmetic (intentional), millisecond calculations, summing large `int` arrays.

---

## 2.11 Floating-point numbers

### What it is
`float` and `double` follow **IEEE 754** binary floating point: sign × binary fraction (mantissa) × 2^exponent. Most decimal fractions (0.1, 0.2) have no exact binary form — as 1/3 has no exact decimal form — so the nearest representable value is stored.

[`code/FloatingPoint.java`](code/FloatingPoint.java):
```java
System.out.println("0.1 + 0.2 = " + (0.1 + 0.2));
System.out.println("0.1 + 0.2 == 0.3: " + (0.1 + 0.2 == 0.3));
System.out.println("|diff| < 1e-9: " + (Math.abs((0.1 + 0.2) - 0.3) < 1e-9));
double nan = 0.0 / 0;
System.out.println("NaN == NaN: " + (nan == nan) + ", Double.isNaN: " + Double.isNaN(nan));
System.out.println("-0.0 == 0.0: " + (-0.0 == 0.0) + ", Double.compare(-0.0, 0.0): " + Double.compare(-0.0, 0.0));
double total = 0;
for (int i = 0; i < 10; i++) total += 0.1;
BigDecimal exact = new BigDecimal("0.1").add(new BigDecimal("0.2"));
```
Output:
```text
0.1 + 0.2 = 0.30000000000000004
0.1 + 0.2 == 0.3: false
|diff| < 1e-9: true
0.1f + 0.2f = 0.3
1.0 - 0.9 = 0.09999999999999998
NaN == NaN: false, Double.isNaN: true
-0.0 == 0.0: true, Double.compare(-0.0, 0.0): -1
1e308 * 10 = Infinity, Math.sqrt(-1) = NaN
16777217 as float = 1.6777216E7  (2^24 + 1 not representable)
0.1 added ten times = 0.9999999999999999
BigDecimal 0.1 + 0.2 = 0.3, new BigDecimal(0.1) = 0.1000000000000000055511151231257827021181583404541015625
Math.round(2.5)=3 Math.round(-2.5)=-2 Math.floor(-2.5)=-3.0 Math.ceil(-2.5)=-2.0
```
(`0.1f + 0.2f` *prints* `0.3` only because `float`'s shorter printed form hides the error.)

### Special values
| Value | Produced by | Test with |
|---|---|---|
| `Infinity`, `-Infinity` | overflow (`1e308 * 10`), `1.0 / 0` | `Double.isInfinite(x)` |
| `NaN` | `0.0 / 0`, `Math.sqrt(-1)` | `Double.isNaN(x)` (or `x != x`) — **never** `x == Double.NaN` (NaN equals nothing, not even itself) |
| `-0.0` | `-1.0 / Double.POSITIVE_INFINITY`, rounding tiny negatives | `== 0.0` is true, but `Double.compare`/`Double.equals` distinguish it |

### How to handle it
1. **Compare with a tolerance**: `Math.abs(a - b) < 1e-9` (choose it for your domain).
2. **Money**: `long` minor units (cents) or `BigDecimal` from a **String** (`new BigDecimal("0.1")`) or `BigDecimal.valueOf(0.1)` — `new BigDecimal(0.1)` captures the binary error exactly, as shown.
3. **Rounding**: `Math.round` rounds half **up toward positive infinity** (`-2.5 → -2`, `-0.5 → 0`); `Math.floor`/`ceil` return `double`. For decimal rounding modes (half-even — round a trailing 5 to the nearest even digit, as banks do) use `BigDecimal.setScale`.
4. **Loops**: never compare a `double` loop counter with `==` — ten additions of 0.1 give 0.9999999999999999.
5. Since Java 17 (JEP 306) all floating-point arithmetic is strict IEEE 754 everywhere, so results are reproducible; `strictfp` is obsolete.

### In the interview
**Q: Why is `0.1 + 0.2 != 0.3`?** Neither is exactly representable in binary; the approximations sum to 0.30000000000000004. Compare with a tolerance, or use `BigDecimal` for exact decimals such as money.
- *Check for NaN?* `Double.isNaN(x)`; `x != x` also works.
- *Why not `new BigDecimal(0.1)`?* It converts the already-inexact double; use the String constructor or `BigDecimal.valueOf`.

---

## 2.12 Strings and text blocks

### What it is
A **`String`** is an object holding an immutable sequence of characters, with special support: literals (`"hi"`), `+` concatenation, and a pool of shared literals. Full API, `StringBuilder` and formatting: [Chapter 7](../07-core-apis/README.md).

[`code/StringBasics.java`](code/StringBasics.java):
```java
String message = "Hello" + ", " + "Ada" + "!";
System.out.println(message + " length=" + message.length());
System.out.println("charAt(0)=" + message.charAt(0) + " indexOf(\"Ada\")=" + message.indexOf("Ada")
        + " substring(7, 10)=" + message.substring(7, 10));

String s = "java";
s.toUpperCase();                        // result thrown away: Strings are immutable
System.out.println("after s.toUpperCase(): " + s);
s = s.toUpperCase();                    // reassign the variable to the new String
System.out.println("after s = s.toUpperCase(): " + s);

String a = "hi", b = "hi", c = new String("hi");
System.out.println("a == b: " + (a == b) + " (same pooled literal), a == c: " + (a == c)
        + ", a.equals(c): " + a.equals(c) + ", \"HI\".equalsIgnoreCase(a): " + "HI".equalsIgnoreCase(a));
System.out.println("String.valueOf(42) + 1 = " + String.valueOf(42) + 1
        + ", Integer.parseInt(\"42\") + 1 = " + (Integer.parseInt("42") + 1));
System.out.println("'a' + 'b' = " + ('a' + 'b') + ", \"\" + 'a' + 'b' = " + ("" + 'a' + 'b'));
```
Output:
```text
Hello, Ada! length=11
charAt(0)=H indexOf("Ada")=7 substring(7, 10)=Ada
upper=HELLO, ADA! contains Ada? true
after s.toUpperCase(): java
after s = s.toUpperCase(): JAVA
a == b: true (same pooled literal), a == c: false, a.equals(c): true, "HI".equalsIgnoreCase(a): true
compareTo: -1, isBlank: true, strip: [pad], repeat: ababab
String.valueOf(42) + 1 = 421, Integer.parseInt("42") + 1 = 43
'a' + 'b' = 195, "" + 'a' + 'b' = ab
null concatenated: null
```

### Key facts
| Fact | Consequence |
|---|---|
| **Immutable** | Methods return a *new* string — assign the result. Safe to share between threads and as `HashMap` keys. |
| **String pool** — identical literals share one object | `"hi" == "hi"` is `true`; `new String("hi")` is a distinct object. Use `equals`, never `==`. |
| Indexes are **0-based**; `substring(begin, end)` excludes `end` | `"Hello".substring(1, 3)` is `"el"`, `"abc".substring(1)` is `"bc"`; out of range throws `StringIndexOutOfBoundsException`. |
| `+` with any `String` operand concatenates | Values go through `String.valueOf`; `null` becomes `"null"`. Two `char`s add **numerically** (`'a' + 'b' == 195`). |
| Concatenating in a loop creates many objects | Use `StringBuilder` (mutable, growable string) in loops ([Chapter 7](../07-core-apis/README.md)). |
| Converting | `String.valueOf(x)` / `"" + x` to text; `Integer.parseInt`, `Double.parseDouble` from text (throw `NumberFormatException` on bad input). |

Most-used methods (version for newer ones): `length`, `charAt`, `indexOf`, `substring`, `contains`, `startsWith`/`endsWith`, `toUpperCase`/`toLowerCase`, `trim`, `strip` (11), `isEmpty`, `isBlank` (11), `repeat` (11), `lines` (11), `replace`, `split`, `equals`, `equalsIgnoreCase`, `compareTo`, `String.join`, `formatted` (15).

### Text blocks (Java 15)
A **text block** (JEP 378, final in Java 15) is a multi-line literal opened by `"""` plus a line break and closed by `"""`. It is an ordinary `String`.

[`code/TextBlocks.java`](code/TextBlocks.java):
```java
String json = """
        {
          "name": "Ada",
          "langs": ["Java", "SQL"]
        }
        """;
System.out.print(json);

String sql = """
    SELECT id, name
    FROM users
    WHERE active = true\
    """;                                     // \ at line end: no newline there
System.out.println("[" + sql + "]");

String spaces = """
    trailing kept:\s
    quote: \""" inside
    """;
System.out.print(spaces.replace(' ', '.'));

String html = """
    <p>Hello, %s!</p>
    """.formatted("World");
```
Output:
```text
{
  "name": "Ada",
  "langs": ["Java", "SQL"]
}
[SELECT id, name
FROM users
WHERE active = true]
trailing.kept:.
quote:.""".inside
<p>Hello, World!</p>
lines in json: 4
```
How the compiler processes it:
1. Content starts on the line **after** the opening `"""` (text on that line is a compile error).
2. **Incidental indentation** is removed: the smallest indentation among non-blank lines *and the closing `"""` line* is stripped from every line. Move the closing delimiter left to keep more indentation.
3. Trailing spaces are stripped; line endings become `\n` on every OS.
4. Escapes are processed last. Two new ones: `\<line-end>` joins lines (no newline), `\s` is a space that survives stripping.
5. Closing `"""` on its own line → the string ends with `\n`; directly after text → no trailing newline.

Use for embedded SQL, JSON, HTML and test fixtures. Indentation traps: OCP [text blocks section](../../../../ocp-java21-study/chapters/01-building-blocks/README.md).

### Common mistakes
- `if (input == "yes")` compares identity; use `"yes".equals(input)` (also null-safe, unlike `input.equals("yes")`).
- `s.trim();` without assigning the result.
- `"Total: " + a + b` with numbers concatenates both (`"Total: 12"`); write `"Total: " + (a + b)`.

---

## 2.13 Console output and input

### Output: `print`, `println`, `printf`
`System.out` is a `PrintStream` connected to **standard output** (terminal by default); `System.err` to **standard error**, a separate stream (shown in red by many IDEs) for diagnostics that should not mix with data output.

[`code/Formatting.java`](code/Formatting.java):
```java
System.out.print("print has no newline; ");
System.out.println("println adds one.");
System.out.printf("%-10s|%5d|%8.2f|%n", "Coffee", 3, 4.5);
System.out.printf("%-10s|%05d|%8.3e|%n", "Padded", 42, 12345.678);
System.out.printf("%s %b %c %x %o %%%n", "str", true, 'J', 255, 8);
System.out.printf(Locale.GERMANY, "German: %,.2f%n", 1234567.891);
System.out.printf(Locale.US, "US:     %,.2f%n", 1234567.891);
String line = String.format("Total: %.2f", qty * price);
String line2 = "Items: %d".formatted(qty);         // Java 15
System.err.println("(this line goes to standard error)");
```
Output:
```text
print has no newline; println adds one.
Coffee    |    3|    4.50|
Padded    |00042|1.235e+04|
str true J ff 10 %
German: 1.234.567,89
US:     1,234,567.89
Total: 13.50 / Items: 3
(this line goes to standard error)
```

| Specifier | Meaning | Flags/width examples |
|---|---|---|
| `%s` | any value via `toString()` (every object's text form) | `%-10s` left-align in 10 chars |
| `%d` | integer (`int`, `long`, `BigInteger`) | `%5d` width 5, `%05d` zero-pad, `%,d` grouping |
| `%f`, `%e` | decimal, scientific | `%.2f` two decimals (rounds half-up) |
| `%b`, `%c` | boolean, char | |
| `%x`, `%o` | hex, octal | `%08X` |
| `%n` | platform line separator (`\n` Linux/macOS, `\r\n` Windows) | prefer over `\n` in `printf` |
| `%%` | a literal `%` | |

Formatting is **locale-sensitive**: the default locale (language-and-region setting) decides decimal and grouping separators, so tests and machine-readable output should pass an explicit `Locale` (`Locale.ROOT`, `Locale.US`). A mismatched type (`%d` with a `double`) throws `IllegalFormatConversionException` at run time.

### Input: four options
| Option | Best for | Notes |
|---|---|---|
| `Scanner` (`java.util`) | Beginner programs, parsing numbers and words | Splits input into whitespace-separated tokens; `nextInt`, `nextDouble`, `next`, `nextLine`, `hasNextInt`… Slow for very large input. |
| `IO.readln(prompt)` (`java.lang.IO`, **Java 25**) | Simplest line input | Returns a line (`null` at end of input); pairs with `IO.println`. |
| `BufferedReader` over `InputStreamReader(System.in)` | Fast line reading (competitive programming, large input) | `readLine()` returns `null` at end; parse numbers yourself; throws `IOException`. |
| `System.console()` → `java.io.Console` | Passwords (`readPassword` does not echo) | `null` when there is no interactive terminal (IDE consoles, input redirected from a file with `<`) — always check; a `null` in your IDE is not a bug. |

**One reader per program**: each buffers (reads ahead from) `System.in`, so a `BufferedReader` created after a `Scanner` misses what the `Scanner` already consumed.

Deterministic `Scanner` demo reading from a `String` ([`code/ScannerDemo.java`](code/ScannerDemo.java)); a real program uses `new Scanner(System.in)`:
```java
String fakeKeyboard = """
        Ada Lovelace
        36
        1.5 2.5 3.0
        done
        """;
Scanner in = new Scanner(fakeKeyboard);
String fullName = in.nextLine();          // whole line
int age = in.nextInt();                   // one token -> int
in.nextLine();                            // consume the rest of the "36" line!
double sum = 0;
while (in.hasNextDouble()) sum += in.nextDouble();
String word = in.next();                  // next token

Scanner trap = new Scanner("42\nAlan Turing\n");
int n = trap.nextInt();
String afterInt = trap.nextLine();        // returns the EMPTY rest of the first line

Scanner bad = new Scanner("forty-two");
if (!bad.hasNextInt()) System.out.println("not an int: " + bad.next());
```
Output:
```text
name=Ada Lovelace, age=36, sum=7.0, word=done
n=42, nextLine after nextInt=[], then=[Alan Turing]
not an int: forty-two
```
**The `nextInt` + `nextLine` trap:** `nextInt()` leaves the line break, so the next `nextLine()` returns the empty remainder. Call an extra `nextLine()` after `nextInt()`, or read every line and `Integer.parseInt` it. `nextInt()` on non-numeric input throws `InputMismatchException` — guard with `hasNextInt()`.

[`code/Interactive.java`](code/Interactive.java) takes the reading style as an argument (`java -cp out ch02.io.Interactive scanner|io|reader|console`). The `console` mode, run in a terminal:
```java
Console console = System.console();          // null if not an interactive terminal
if (console == null) {
    System.out.println("No interactive console (IDE or redirected input).");
    return;
}
char[] secret = console.readPassword("Password (not echoed): ");
if (secret == null) return;                   // end of input (Ctrl+D)
System.out.println("Password length: " + secret.length);
Arrays.fill(secret, ' ');                     // wipe it from memory
```
```text
Password (not echoed):
Password length: 7
```
With redirected input (`… console < file.txt`) it prints `No interactive console (IDE or redirected input).` Passwords come as `char[]`, not `String`, so you can wipe them (a `String` lingers until garbage-collected). (JDK 22–24 `System.console()` could return a `Console` even when redirected; JDK 25 restored the older default, and JDK 26 made `null` the rule whenever input or output is redirected.)

`java.lang.IO` also works in ordinary classes ([`code/IOClassDemo.java`](code/IOClassDemo.java)):
```java
IO.print("IO.print, ");
IO.println("IO.println");
IO.println(42);
IO.println();
IO.println("done");
```
Output:
```text
IO.print, IO.println
42

done
```

Command-line arguments (`String[] args`) are the other common input channel — [Chapter 4](../04-methods-and-arrays/README.md).

---

## Chapter TL;DR
- Eight primitives: `byte short int long` (8/16/32/64-bit signed), `float double` (IEEE 754), `char` (16-bit unsigned UTF-16 unit), `boolean`. Everything else is a reference (or `null`).
- Locals must be assigned before use; fields get defaults. `final` = assign once (the object can still change). `var` (Java 10) infers local types; the type is still fixed.
- Literals: `L` long, `f` float, `0x`/`0b`/leading-`0` hex/binary/octal, `_` separators.
- Widening is automatic (but `int/long → float` can lose precision); narrowing needs a cast and truncates/wraps. `byte/short/char` promote to `int`; compound assignment hides a cast.
- Integer `/` truncates; `%` follows the dividend's sign (`Math.floorMod` otherwise); integer `/0` throws, floating `/0` gives Infinity/NaN.
- `&&`/`||` short-circuit; `&`/`|` do not. `==` on references compares identity — use `equals`.
- Precedence: unary → `* / %` → `+ -` → shifts → relational → equality → `&` `^` `|` → `&&` → `||` → `?:` → assignment.
- Overflow wraps silently: `long`, `Math.*Exact`, `BigInteger`. Floating point is approximate: tolerance compares; money in `BigDecimal`/cents.
- `String` is immutable and pooled; text blocks (Java 15) strip incidental indentation.
- Output with `println`/`printf` (locale-aware); input with `Scanner` (mind `nextLine`), `IO.readln` (Java 25), `BufferedReader`, or `Console` for passwords — one reader per program.
- Exam traps: [OCP Chapter 1](../../../../ocp-java21-study/chapters/01-building-blocks/README.md), [OCP Chapter 2](../../../../ocp-java21-study/chapters/02-operators/README.md). Next: [Chapter 3 — Control flow](../03-control-flow/README.md).
