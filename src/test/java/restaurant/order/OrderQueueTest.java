package restaurant.order;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class OrderQueueTest {

    @TempDir
    Path directory;

    @Test
    void subsystemAWorksAndReloadsIndependently() {
        OrderQueue orders = new OrderQueue(directory);

        orders.reserve("1", "Burger", 2, "Astana");
        orders.reserve("1", "Burger", 2, "Astana");

        assertEquals(
                10L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );

        assertEquals(1, orders.all().size());

        orders.confirm("1");
        orders.confirm("1");
        orders.reserve("1", "Burger", 2, "Astana");

        assertEquals(
                OrderStatus.CONFIRMED,
                orders.find("1").getStatus()
        );

        assertEquals(
                10L,
                orders.stock().get("bun").longValue()
        );

        assertThrows(
                IllegalStateException.class,
                () -> orders.cancel("1")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> orders.reserve("1", "Pizza", 1, "Astana")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> orders.confirm("missing")
        );

        orders.cancel("missing");
    }

    @Test
    void rejectedAndCancelledOrdersRestoreCorrectStock() {
        OrderQueue orders = new OrderQueue(directory);

        assertThrows(
                OrderException.class,
                () -> orders.reserve("1", "Burger", 13, "Astana")
        );

        assertEquals(
                OrderStatus.REJECTED,
                orders.find("1").getStatus()
        );

        orders.cancel("1");

        assertThrows(
                OrderException.class,
                () -> orders.reserve("1", "Burger", 13, "Astana")
        );

        orders.reserve("2", "Pizza", 1, "Astana");
        orders.cancel("2");
        orders.cancel("2");

        assertEquals(
                new InventoryChecker().initialStock(),
                new OrderQueue(directory).stock()
        );

        assertThrows(
                OrderException.class,
                () -> orders.reserve("2", "Pizza", 1, "Astana")
        );
    }
}