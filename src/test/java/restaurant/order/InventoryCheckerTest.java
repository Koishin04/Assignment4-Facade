package restaurant.order;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class InventoryCheckerTest {

    @Test
    void recipesAndStockAreCalculated() {
        InventoryChecker inventory = new InventoryChecker();

        assertEquals(
                2L,
                inventory.recipe("Burger", 2).get("bun").longValue()
        );

        assertEquals(
                4L,
                inventory.recipe("Pizza", 2).get("cheese").longValue()
        );

        assertEquals(
                2L,
                inventory.recipe("Pasta", 2).get("pasta").longValue()
        );

        assertTrue(
                inventory.enough(
                        inventory.initialStock(),
                        inventory.recipe("Pizza", 2)
                )
        );

        assertFalse(
                inventory.enough(
                        inventory.initialStock(),
                        inventory.recipe("Burger", 13)
                )
        );

        assertFalse(
                inventory.enough(
                        Map.of(),
                        inventory.recipe("Pasta", 1)
                )
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.recipe("Soup", 1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> inventory.recipe("Burger", -1)
        );

        assertEquals(
                4294967294L,
                inventory.recipe("Pizza", Integer.MAX_VALUE)
                        .get("cheese").longValue()
        );
    }

    @Test
    void cancelledReservationsDoNotConsumeStock() {
        InventoryChecker inventory = new InventoryChecker();
        Order order = new Order("1", "Burger", 2, "Astana");

        order.reserve(inventory.recipe("Burger", 2));

        assertEquals(
                10L,
                inventory.available(List.of(order)).get("bun").longValue()
        );

        order.cancel();

        assertEquals(
                12L,
                inventory.available(List.of(order)).get("bun").longValue()
        );
    }
}