package restaurant.chain;

import restaurant.facade.*;
import restaurant.order.*;
import restaurant.kitchen.*;
import restaurant.billing.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class PersistenceReloadTest {

    @TempDir
    Path directory;

    @Test
    void restartAfterAAndBCompletesWithoutDuplicateReservations() {
        String burger = new RestaurantFacade(directory)
                .start("Burger", 2, "A", true, 1)
                .getId();

        String pizza = new RestaurantFacade(directory)
                .start("Pizza", 1, "A", true, 2)
                .getId();

        RestaurantFacade restarted = new RestaurantFacade(directory);

        restarted.recoverAll();
        restarted.recoverAll();

        ChainProgressRepository chains =
                new ChainProgressRepository(directory);

        assertEquals(
                ChainStatus.COMPLETED,
                chains.find(burger).getStatus()
        );

        assertEquals(
                ChainStatus.COMPLETED,
                chains.find(pizza).getStatus()
        );

        assertEquals(
                10L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );

        assertEquals(
                9L,
                new OrderQueue(directory).stock().get("dough").longValue()
        );

        assertEquals(
                2,
                new CheckoutRepository(directory).findAll().size()
        );
    }

    @Test
    void savedPaymentIsNotDeclinedOrChargedAgainOnRecovery() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 2, "A", false, 2)
                .getId();

        Checkout paid = new Checkout(id, 5000, "A");
        paid.pay();

        new CheckoutRepository(directory).save(paid);

        assertThrows(
                IllegalStateException.class,
                () -> new AdminFacade(directory).cancel(id)
        );

        assertEquals(
                ChainStatus.COMPLETED,
                new RestaurantFacade(directory).resume(id).getStatus()
        );

        assertEquals(
                "DEL-" + id,
                new CheckoutRepository(directory).find(id).getDeliveryId()
        );

        assertEquals(
                1,
                new CheckoutRepository(directory).findAll().size()
        );
    }

    @Test
    void partialFinalizationResumesSafely() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 2, "A", true, 2)
                .getId();

        new DeliveryDispatcher(directory)
                .complete(id, "Burger", 2, "A", true);

        new OrderQueue(directory).confirm(id);

        assertEquals(
                ChainStatus.COMPLETED,
                new RestaurantFacade(directory).resume(id).getStatus()
        );

        assertEquals(
                KitchenStatus.CONFIRMED,
                new KitchenRepository(directory).find(id).getStatus()
        );

        assertEquals(
                10L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );
    }

    @Test
    void compensationResumesAfterAWasAlreadyCancelled() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 2, "A", true, 1)
                .getId();

        ChainProgressRepository chains =
                new ChainProgressRepository(directory);

        ChainProgress chain = chains.find(id);

        chain.move(ChainStatus.COMPENSATING_A, "B_FAILED");
        chains.save(chain);

        new OrderQueue(directory).cancel(id);

        new RestaurantFacade(directory).resume(id);
        new RestaurantFacade(directory).resume(id);

        assertEquals(
                ChainStatus.COMPENSATED,
                chains.find(id).getStatus()
        );

        assertEquals(
                12L,
                new OrderQueue(directory).stock().get("bun").longValue()
        );
    }
}