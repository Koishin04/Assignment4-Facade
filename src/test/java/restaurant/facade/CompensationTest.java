package restaurant.facade;

import restaurant.order.*;
import restaurant.kitchen.*;
import restaurant.billing.*;
import restaurant.chain.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CompensationTest {

    @TempDir
    Path directory;

    @Test
    void busyStationsCompensateOnlyA() {
        new AdminFacade(directory).occupyAllStations();

        ChainProgress result = new RestaurantFacade(directory)
                .start("Burger", 2, "A", true, 3);

        assertEquals(ChainStatus.COMPENSATED, result.getStatus());

        assertEquals(
                KitchenStatus.REJECTED,
                new KitchenRepository(directory)
                        .find(result.getId())
                        .getStatus()
        );

        assertNull(
                new CheckoutRepository(directory).find(result.getId())
        );

        assertFalse(
                result.getEvents().contains("B_CANCELLED")
        );

        assertIngredientsWereRestored(result.getId());
    }

    @Test
    void declinedPaymentCompensatesBBeforeA() {
        ChainProgress result = new RestaurantFacade(directory)
                .start("Burger", 2, "A", false, 3);

        String id = result.getId();

        assertEquals(ChainStatus.COMPENSATED, result.getStatus());

        assertEquals(
                CheckoutStatus.FAILED,
                new CheckoutRepository(directory).find(id).getStatus()
        );

        assertEquals(
                KitchenStatus.CANCELLED,
                new KitchenRepository(directory).find(id).getStatus()
        );

        int b = result.getEvents().indexOf("B_CANCELLED");
        int a = result.getEvents().indexOf("A_CANCELLED");

        assertTrue(b >= 0 && a > b);

        new RestaurantFacade(directory).resume(id);

        assertIngredientsWereRestored(id);

        assertFalse(
                new CookingStationAllocator(directory).isBusy("Burger")
        );
    }

    @Test
    void recoveryDetectsSavedPaymentFailureWithoutNewChainMarker() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 2, "A", false, 2)
                .getId();

        assertThrows(
                PaymentException.class,
                () -> new DeliveryDispatcher(directory)
                        .complete(id, "Burger", 2, "A", false)
        );

        assertEquals(
                ChainStatus.COMPENSATED,
                new RestaurantFacade(directory).resume(id).getStatus()
        );

        assertIngredientsWereRestored(id);
    }

    @Test
    void recoveryDetectsSavedBFailureWithoutNewChainMarker() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 2, "A", true, 1)
                .getId();

        new AdminFacade(directory).occupyAllStations();

        assertThrows(
                KitchenException.class,
                () -> new CookingStationAllocator(directory)
                        .prepare(id, "Burger", 2)
        );

        assertEquals(
                ChainStatus.COMPENSATED,
                new RestaurantFacade(directory).resume(id).getStatus()
        );

        assertIngredientsWereRestored(id);
    }

    @Test
    void cancellationResumesAfterBWasAlreadySaved() {
        RestaurantFacade restaurant = new RestaurantFacade(directory);

        String id = restaurant
                .start("Burger", 2, "A", false, 2)
                .getId();

        ChainProgressRepository chains =
                new ChainProgressRepository(directory);

        ChainProgress chain = chains.find(id);

        chain.move(ChainStatus.COMPENSATING_B, "C_FAILED");
        chains.save(chain);

        new CookingStationAllocator(directory).cancel(id);

        assertEquals(
                OrderStatus.RESERVED,
                new OrderRepository(directory).find(id).getStatus()
        );

        new RestaurantFacade(directory).resume(id);

        assertIngredientsWereRestored(id);

        int count = 0;

        for (String event : chains.find(id).getEvents()) {
            if (event.equals("B_CANCELLED")) {
                count++;
            }
        }

        assertEquals(1, count);
    }

    private void assertIngredientsWereRestored(String id) {
        assertEquals(
                OrderStatus.CANCELLED,
                new OrderRepository(directory).find(id).getStatus()
        );

        assertEquals(
                new InventoryChecker().initialStock(),
                new OrderQueue(directory).stock()
        );
    }
}