package lld.ch06.testing;

import java.time.Clock;
import java.util.List;
import java.util.Optional;

/**
 * BEFORE: how NOT to write tests. It "passes", but:
 *  - tests several concepts at once, so a failure doesn't say what broke
 *  - uses the real system clock (not Repeatable)
 *  - prints instead of asserting (not Self-validating: a human must read the output)
 *  - cryptic name and magic numbers
 */
public class BeforeStyleTest {
    @Test void test1() {
        var c = new Checkout(sku -> 12_000L, code -> Optional.empty(), r -> {}, e -> {}, Clock.systemDefaultZone());
        long a = c.quote(List.of(new Checkout.Line("X", 3)), null);
        long b = c.quote(List.of(new Checkout.Line("X", 3)), "NOPE");
        var r = c.checkout(List.of(new Checkout.Line("X", 1)), null, "z");
        System.out.println("        a=" + a + " b=" + b + " r=" + r + "  (is this right? someone must check by eye)");
    }
}
