// tools_run: skip   (JUnit test — run with code/run_tests.sh, see 18.3)
package ch18.shop;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.DynamicTest.dynamicTest;

import java.math.BigDecimal;
import java.util.stream.Stream;
import org.junit.jupiter.api.DynamicTest;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.TestFactory;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.MethodSource;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

class DiscountsTest {

    @ParameterizedTest(name = "{0} is a valid code")
    @ValueSource(strings = {"SAVE10", "SAVE25", "WELCOME99"})
    void validCodes(String code) {
        assertTrue(Discounts.isValidCode(code));
    }

    @ParameterizedTest(name = "[{index}] {0} is rejected")
    @NullAndEmptySource                                   // adds null and ""
    @ValueSource(strings = {"save10", "SAV10", "SAVE1", "SAVE100"})
    void invalidCodes(String code) {
        assertFalse(Discounts.isValidCode(code));
    }

    @ParameterizedTest(name = "{0} on {1} -> {2}%")
    @CsvSource({
            "SAVE10,  20.00, 10",
            "SAVE25,  99.99,  0",     // boundary: just below 100
            "SAVE25, 100.00, 25",     // boundary: exactly 100
            "SAVE99, 500.00,  0",     // well-formed but unknown
    })
    void percentForCode(String code, BigDecimal subtotal, int expected) {   // strings converted to BigDecimal/int
        assertEquals(expected, Discounts.percentFor(code, subtotal));
    }

    static Stream<Arguments> roundingCases() {
        return Stream.of(
                Arguments.of("10.00", 10, "9.00"),
                Arguments.of("0.05", 50, "0.03"),              // 0.025 rounds half-up to 0.03
                Arguments.of("102.50", 25, "76.88"),           // 76.875 -> 76.88
                Arguments.of("19.99", 100, "0.00"));
    }

    @ParameterizedTest
    @MethodSource("roundingCases")
    void applyRoundsHalfUpToCents(String amount, int percent, String expected) {
        assertEquals(new BigDecimal(expected), Discounts.apply(new BigDecimal(amount), percent));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 101})
    void applyRejectsBadPercent(int percent) {
        var e = assertThrows(IllegalArgumentException.class, () -> Discounts.apply(BigDecimal.TEN, percent));
        assertTrue(e.getMessage().contains(String.valueOf(percent)));
    }

    @RepeatedTest(3)
    void applyIsDeterministic() {
        assertEquals(new BigDecimal("9.00"), Discounts.apply(BigDecimal.TEN, 10));
    }

    @TestFactory
    Stream<DynamicTest> everyPercentKeepsTheResultWithinRange() {
        return Stream.of(0, 33, 50, 99, 100).map(p -> dynamicTest("percent " + p, () -> {
            BigDecimal r = Discounts.apply(new BigDecimal("80.00"), p);
            assertTrue(r.signum() >= 0 && r.compareTo(new BigDecimal("80.00")) <= 0, () -> "got " + r);
        }));
    }
}
