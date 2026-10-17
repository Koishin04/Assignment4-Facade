package restaurant.facade;

import restaurant.chain.*;
import restaurant.kitchen.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class AdminFacadeTest {

    @TempDir
    Path directory;

    @Test
    void demoStationsAndInspectionWorkAfterRestart() {
        AdminFacade admin = new AdminFacade(directory);

        admin.occupyAllStations();
        admin.occupyAllStations();

        assertEquals(
                3,
                new KitchenRepository(directory).findAll().size()
        );

        assertTrue(
                new AdminFacade(directory)
                        .inspect()
                        .contains("Burger station busy: true")
        );

        admin.releaseDemoStations();
        admin.releaseDemoStations();

        ChainProgress chain = new RestaurantFacade(directory)
                .start("Pizza", 1, "Astana", true, 3);

        String view = admin.inspect();

        assertTrue(view.contains("ORDER " + chain.getId()));
        assertTrue(view.contains("CHECKOUT " + chain.getId()));
        assertTrue(view.contains("COMPLETED"));
        assertTrue(view.contains("3200 KZT"));

        admin.finishKitchen(chain.getId());

        assertFalse(
                new CookingStationAllocator(directory).isBusy("Pizza")
        );
    }

    @Test
    void manualCompensationUsesTheSameWorkflow() {
        String id = new RestaurantFacade(directory)
                .start("Burger", 1, "A", true, 2)
                .getId();

        AdminFacade admin = new AdminFacade(directory);

        assertEquals(
                ChainStatus.COMPENSATED,
                admin.cancel(id).getStatus()
        );

        assertEquals(
                ChainStatus.COMPENSATED,
                admin.cancel(id).getStatus()
        );

        assertFalse(
                new CookingStationAllocator(directory).isBusy("Burger")
        );
    }
}