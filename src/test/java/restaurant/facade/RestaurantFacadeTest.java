package restaurant.facade;

import restaurant.order.*;
import restaurant.kitchen.*;
import restaurant.billing.*;
import restaurant.chain.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class RestaurantFacadeTest {

    @TempDir
    Path directory;

    @Test
    void successfulOrderIsFullyPersisted() {
        RestaurantFacade restaurant = new RestaurantFacade(directory);

        ChainProgress chain = restaurant.start(
                "Burger", 2, "Astana", true, 3
        );

        String id = chain.getId();

        assertEquals(ChainStatus.COMPLETED, chain.getStatus());

        assertEquals(
                OrderStatus.CONFIRMED,
                new OrderRepository(directory).find(id).getStatus()
        );

        assertEquals(
                KitchenStatus.CONFIRMED,
                new KitchenRepository(directory).find(id).getStatus()
        );

        assertEquals(
                CheckoutStatus.DISPATCHED,
                new CheckoutRepository(directory).find(id).getStatus()
        );

        assertEquals(
                5000,
                new CheckoutRepository(directory).find(id).getTotal()
        );

        assertEquals(
                ChainStatus.COMPLETED,
                new RestaurantFacade(directory).resume(id).getStatus()
        );

        assertEquals(
                10L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );

        assertThrows(
                IllegalStateException.class,
                () -> restaurant.cancel(id)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurant.resume("missing")
        );
    }

    @Test
    void insufficientIngredientsStopBeforeB() {
        RestaurantFacade restaurant = new RestaurantFacade(directory);

        ChainProgress chain = restaurant.start(
                "Burger", 13, "Astana", true, 3
        );

        assertEquals(ChainStatus.REJECTED, chain.getStatus());

        assertNull(
                new KitchenRepository(directory).find(chain.getId())
        );

        assertNull(
                new CheckoutRepository(directory).find(chain.getId())
        );

        assertEquals(
                12L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );

        assertEquals(
                ChainStatus.REJECTED,
                restaurant.cancel(chain.getId()).getStatus()
        );
    }

    @Test
    void badInputDoesNotStartAChain() {
        RestaurantFacade restaurant = new RestaurantFacade(directory);

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurant.start("Burger", 1, "A", true, 0)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurant.start("Burger", 0, "A", true, 3)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurant.start("Soup", 1, "A", true, 3)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> restaurant.start("Burger", 1, " ", true, 3)
        );

        assertTrue(
                new ChainProgressRepository(directory).findAll().isEmpty()
        );
    }
}