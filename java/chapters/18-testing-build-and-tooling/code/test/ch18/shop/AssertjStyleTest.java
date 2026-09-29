// tools_run: skip   (JUnit + AssertJ test — run with code/run_tests.sh, see 18.6)
package ch18.shop;

import static org.assertj.core.api.Assertions.*;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import org.assertj.core.api.SoftAssertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

class AssertjStyleTest {

    static Cart sampleCart() {
        Cart cart = new Cart();
        cart.add(new Item("BK-1", "Java book", new BigDecimal("45.00"), 2));
        cart.add(new Item("MUG", "Mug", new BigDecimal("12.50"), 1));
        return cart;
    }

    @Test
    void collections() {
        assertThat(sampleCart().items())
                .hasSize(2)
                .extracting(Item::sku)
                .containsExactly("BK-1", "MUG");
    }

    @Test
    void bigDecimalsCompareByValue() {
        assertThat(sampleCart().subtotal())
                .isEqualByComparingTo("102.5")               // ignores scale: 102.5 == 102.50
                .isGreaterThan(BigDecimal.ZERO);
    }

    @Test
    void exceptions() {
        assertThatThrownBy(() -> new Item("X", "x", BigDecimal.ONE, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("quantity must be positive: 0");
        assertThatIllegalArgumentException().isThrownBy(() -> Discounts.apply(BigDecimal.ONE, 200));
    }

    @Test
    void recursiveComparisonIgnoringAField() {
        Order actual = new Order("ORD-42", "ann", new BigDecimal("9.99"), Instant.now());
        Order expected = new Order("ignored", "ann", new BigDecimal("9.99"), Instant.EPOCH);
        assertThat(actual).usingRecursiveComparison()
                .ignoringFields("id", "placedAt")
                .isEqualTo(expected);
    }

    @Test
    void softAssertionsCollectAllFailures() {
        Cart cart = sampleCart();
        SoftAssertions.assertSoftly(soft -> {
            soft.assertThat(cart.count()).isEqualTo(3);
            soft.assertThat(cart.isEmpty()).isFalse();
            soft.assertThat(cart.items()).allSatisfy(i -> assertThat(i.quantity()).isPositive());
        });
    }

    @Test
    void writesCsvIntoATemporaryDirectory(@TempDir Path dir) throws IOException {   // deleted after the test
        Order o = new Order("ORD-1", "ann", new BigDecimal("76.88"), Instant.parse("2025-09-16T10:00:00Z"));
        Path csv = OrderExporter.writeCsv(List.of(o), dir);

        assertThat(csv).exists().hasFileName("orders.csv");
        assertThat(Files.readAllLines(csv)).containsExactly(
                "id,customer,total,placed_at",
                "ORD-1,ann,76.88,2025-09-16T10:00:00Z");
    }
}
