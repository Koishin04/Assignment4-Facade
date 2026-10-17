package restaurant.order;

import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {

    @Test
    void orderControlsItsStatus() {
        Order order = new Order("1", "Burger", 2, " Astana ");

        assertEquals("1", order.getId());
        assertEquals("Burger", order.getDish());
        assertEquals(2, order.getQuantity());
        assertEquals("Astana", order.getAddress());
        assertEquals(OrderStatus.NEW, order.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> order.confirm()
        );

        order.reserve(Map.of("bun", 2L));
        order.reserve(Map.of("bun", 2L));

        assertEquals(
                2L,
                order.getReservation().get("bun").longValue()
        );

        order.confirm();
        order.confirm();

        assertEquals(OrderStatus.CONFIRMED, order.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> order.cancel()
        );

        assertThrows(
                IllegalStateException.class,
                () -> order.reject()
        );
    }

    @Test
    void cancellationAndRejectionAreDifferent() {
        Order order = new Order("1", "Pizza", 1, "Astana");

        order.reserve(Map.of("dough", 1L));
        order.cancel();
        order.cancel();

        assertEquals(OrderStatus.CANCELLED, order.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> order.reserve(Map.of())
        );

        Order rejected = new Order("2", "Pasta", 1, "Astana");
        rejected.reject();

        assertEquals(OrderStatus.REJECTED, rejected.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> rejected.cancel()
        );

        Order fresh = new Order("3", "Pasta", 1, "Astana");
        fresh.cancel();

        assertEquals(OrderStatus.CANCELLED, fresh.getStatus());
    }

    @Test
    void invalidInputIsRejected() {
        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("1", "Burger", 0, "A")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("1", "Soup", 1, "A")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("", "Burger", 1, "A")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Order("1", "Burger", 1, " ")
        );
    }
}