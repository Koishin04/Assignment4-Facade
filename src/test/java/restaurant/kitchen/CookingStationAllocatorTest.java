package restaurant.kitchen;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class CookingStationAllocatorTest {

    @TempDir
    Path directory;

    @Test
    void stationsAreSpecializedAndPersisted() {
        CookingStationAllocator kitchen =
                new CookingStationAllocator(directory);

        assertEquals(
                "ST-01",
                kitchen.prepare("1", "Burger", 2).getStation()
        );

        assertEquals(
                "ST-02",
                kitchen.prepare("2", "Pizza", 1).getStation()
        );

        assertEquals(
                "ST-03",
                kitchen.prepare("3", "Pasta", 1).getStation()
        );

        assertTrue(
                new CookingStationAllocator(directory).isBusy("Burger")
        );

        assertThrows(
                KitchenException.class,
                () -> kitchen.prepare("4", "Burger", 1)
        );

        assertEquals(
                KitchenStatus.REJECTED,
                kitchen.find("4").getStatus()
        );

        kitchen.cancel("4");
        kitchen.cancel("missing");

        assertThrows(
                KitchenException.class,
                () -> kitchen.prepare("4", "Burger", 1)
        );

        assertEquals(
                "1",
                kitchen.prepare("1", "Burger", 2).getId()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> kitchen.prepare("1", "Pizza", 1)
        );

        kitchen.confirm("1");
        kitchen.finish("1");
        kitchen.finish("1");

        assertFalse(
                new CookingStationAllocator(directory).isBusy("Burger")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> kitchen.confirm("missing")
        );

        kitchen.cancel("2");
        kitchen.cancel("2");

        assertFalse(kitchen.isBusy("Pizza"));
        assertEquals(4, kitchen.all().size());
    }

    @Test
    void readinessRulesAreRealBusinessRules() {
        QualityChecker quality = new QualityChecker();

        quality.check("Burger", 1, "ST-01", false);

        assertThrows(
                KitchenException.class,
                () -> quality.check("Pasta", 1, "ST-01", false)
        );

        assertThrows(
                KitchenException.class,
                () -> quality.check("Burger", 0, "ST-01", false)
        );

        assertThrows(
                KitchenException.class,
                () -> quality.check("Burger", 1, "ST-01", true)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> quality.stationFor("Soup")
        );
    }
}