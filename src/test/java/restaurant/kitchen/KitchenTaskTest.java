package restaurant.kitchen;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class KitchenTaskTest {

    @Test
    void validLifecycleControlsStationOccupation() {
        KitchenTask task = new KitchenTask(
                "1",
                "Burger",
                2,
                "ST-01"
        );

        assertEquals("1", task.getId());
        assertEquals("Burger", task.getDish());
        assertEquals(2, task.getQuantity());
        assertEquals("ST-01", task.getStation());
        assertFalse(task.occupiesStation());

        assertThrows(
                IllegalStateException.class,
                () -> task.finish()
        );

        task.ready();

        assertTrue(task.occupiesStation());

        task.confirm();
        task.confirm();

        assertThrows(
                IllegalStateException.class,
                () -> task.cancel()
        );

        task.finish();
        task.finish();
        task.confirm();

        assertEquals(KitchenStatus.FINISHED, task.getStatus());
        assertFalse(task.occupiesStation());
    }

    @Test
    void cancellationAndRejectionReleaseStation() {
        KitchenTask task = new KitchenTask(
                "1",
                "Pizza",
                1,
                "ST-02"
        );

        task.ready();
        task.cancel();
        task.cancel();

        assertEquals(KitchenStatus.CANCELLED, task.getStatus());

        KitchenTask rejected = new KitchenTask(
                "2",
                "Pasta",
                1,
                "ST-03"
        );

        rejected.reject();

        assertEquals(KitchenStatus.REJECTED, rejected.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> rejected.ready()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new KitchenTask("", "Burger", 1, "ST-01")
        );
    }
}