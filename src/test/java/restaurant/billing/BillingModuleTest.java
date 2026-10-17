package restaurant.billing;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.*;

class BillingModuleTest {

    @TempDir
    Path directory;

    @Test
    void pricesAreWholeTenge() {
        BillingModule billing = new BillingModule();

        assertEquals(5000, billing.total("Burger", 2));
        assertEquals(3200, billing.total("Pizza", 1));
        assertEquals(2700, billing.total("Pasta", 1));

        assertThrows(
                IllegalArgumentException.class,
                () -> billing.total("Soup", 1)
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> billing.total("Burger", 0)
        );

        Checkout paid = new Checkout("1", 2500, "A");

        billing.charge(paid, true);
        billing.charge(paid, false);

        assertEquals(CheckoutStatus.PAID, paid.getStatus());
    }

    @Test
    void subsystemCWorksWithoutAOrB() {
        DeliveryDispatcher dispatcher =
                new DeliveryDispatcher(directory);

        dispatcher.complete("1", "Burger", 2, "Astana", true);

        Checkout saved = new DeliveryDispatcher(directory)
                .complete("1", "Burger", 2, "Astana", false);

        assertEquals(CheckoutStatus.DISPATCHED, saved.getStatus());
        assertEquals("DEL-1", saved.getDeliveryId());
        assertEquals(1, dispatcher.all().size());

        assertThrows(
                IllegalArgumentException.class,
                () -> dispatcher.complete(
                        "1", "Pizza", 1, "Astana", true
                )
        );

        assertThrows(
                PaymentException.class,
                () -> dispatcher.complete(
                        "2", "Pasta", 1, "Astana", false
                )
        );

        assertEquals(
                CheckoutStatus.FAILED,
                dispatcher.find("2").getStatus()
        );

        assertThrows(
                PaymentException.class,
                () -> dispatcher.complete(
                        "2", "Pasta", 1, "Astana", true
                )
        );
    }
}