// tools_run: skip   (JUnit test — run with code/run_tests.sh, see 18.3)
package ch18.shop;

import static org.junit.jupiter.api.Assertions.*;

import java.math.BigDecimal;
import java.util.NoSuchElementException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

@Tag("fast")
@DisplayName("Cart")
class CartTest {

    private Cart cart;                                   // a NEW CartTest instance is created for every test

    @BeforeEach
    void setUp() {
        cart = new Cart();
    }

    @Test
    void newCartIsEmpty() {
        assertTrue(cart.isEmpty());
        assertEquals(0, cart.count());
        assertEquals(new BigDecimal("0.00"), cart.subtotal());
    }

    @Test
    @DisplayName("adding the same SKU twice merges the quantities")
    void addSameSkuMerges() {
        cart.add(new Item("MUG", "Mug", new BigDecimal("12.50"), 1));
        cart.add(new Item("MUG", "Mug", new BigDecimal("12.50"), 2));

        assertAll("merged line",                         // reports EVERY failing check, not just the first
                () -> assertEquals(1, cart.items().size()),
                () -> assertEquals(3, cart.count()),
                () -> assertEquals(new BigDecimal("37.50"), cart.subtotal()));
    }

    @Test
    void removingUnknownSkuThrows() {
        NoSuchElementException e = assertThrows(NoSuchElementException.class, () -> cart.remove("NOPE"));
        assertEquals("not in cart: NOPE", e.getMessage());
    }

    @Test
    void itemsViewIsReadOnly() {
        cart.add(new Item("MUG", "Mug", BigDecimal.ONE, 1));
        assertThrows(UnsupportedOperationException.class, () -> cart.items().clear());
    }

    @Test
    @Disabled("shipping costs not implemented yet")
    void shippingIsFreeAbove50() {
        fail("not written yet");
    }

    @Nested
    @DisplayName("with a book and a mug")
    class WithTwoItems {
        @BeforeEach
        void fill() {                                    // runs AFTER the outer setUp()
            cart.add(new Item("BK-1", "Java book", new BigDecimal("45.00"), 2));
            cart.add(new Item("MUG", "Mug", new BigDecimal("12.50"), 1));
        }

        @Test
        void subtotalAddsLineTotals() {
            assertEquals(new BigDecimal("102.50"), cart.subtotal());
        }

        @Test
        void removeDropsTheWholeLine() {
            cart.remove("BK-1");
            assertEquals(1, cart.count());
            assertFalse(cart.isEmpty());
        }
    }
}
