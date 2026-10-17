package restaurant.billing;

public class BillingModule {

    public long total(String dish, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        long price;

        if ("Burger".equals(dish)) {
            price = 2500;
        } else if ("Pizza".equals(dish)) {
            price = 3200;
        } else if ("Pasta".equals(dish)) {
            price = 2700;
        } else {
            throw new IllegalArgumentException("Unknown dish");
        }

        return price * quantity;
    }

    public void charge(Checkout checkout, boolean approved) {
        if (checkout.getStatus() == CheckoutStatus.PAID
                || checkout.getStatus() == CheckoutStatus.DISPATCHED) {
            return;
        }

        if (checkout.getStatus() == CheckoutStatus.FAILED) {
            throw new PaymentException("Payment already declined");
        }

        if (!approved) {
            checkout.fail();
            throw new PaymentException("Payment declined");
        }

        checkout.pay();
    }
}