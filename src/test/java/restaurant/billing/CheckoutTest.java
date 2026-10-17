package restaurant.billing;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CheckoutTest {

    @Test
    void paymentAndDeliveryHaveLegalTransitions() {
        Checkout checkout = new Checkout("1", 5000, " Astana ");

        assertEquals("1", checkout.getId());
        assertEquals(5000, checkout.getTotal());
        assertEquals("Astana", checkout.getAddress());

        assertThrows(
                IllegalStateException.class,
                () -> checkout.dispatch()
        );

        checkout.pay();
        checkout.pay();
        checkout.dispatch();
        checkout.dispatch();
        checkout.pay();

        assertEquals(
                CheckoutStatus.DISPATCHED,
                checkout.getStatus()
        );

        assertEquals("DEL-1", checkout.getDeliveryId());

        assertThrows(
                IllegalStateException.class,
                () -> checkout.fail()
        );
    }

    @Test
    void failedCheckoutCannotBecomePaid() {
        Checkout checkout = new Checkout("1", 2500, "Astana");

        checkout.fail();

        assertEquals(CheckoutStatus.FAILED, checkout.getStatus());

        assertThrows(
                IllegalStateException.class,
                () -> checkout.pay()
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Checkout("", 1, "A")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Checkout("1", 0, "A")
        );

        assertThrows(
                IllegalArgumentException.class,
                () -> new Checkout("1", 1, " ")
        );
    }
}