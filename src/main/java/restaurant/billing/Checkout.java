package restaurant.billing;

public class Checkout {
    private String id;
    private long total;
    private String address;
    private String deliveryId;
    private CheckoutStatus status = CheckoutStatus.NEW;

    public Checkout() {
    }

    public Checkout(String id, long total, String address) {
        if (id == null || id.isBlank()
                || total <= 0
                || address == null || address.isBlank()) {
            throw new IllegalArgumentException("Invalid checkout");
        }

        this.id = id;
        this.total = total;
        this.address = address.trim();
    }

    public String getId() {
        return id;
    }

    public long getTotal() {
        return total;
    }

    public String getAddress() {
        return address;
    }

    public String getDeliveryId() {
        return deliveryId;
    }

    public CheckoutStatus getStatus() {
        return status;
    }

    public void pay() {
        if (status == CheckoutStatus.PAID
                || status == CheckoutStatus.DISPATCHED) {
            return;
        }

        require(CheckoutStatus.NEW);
        status = CheckoutStatus.PAID;
    }

    public void fail() {
        require(CheckoutStatus.NEW);
        status = CheckoutStatus.FAILED;
    }

    public void dispatch() {
        if (status == CheckoutStatus.DISPATCHED) {
            return;
        }

        require(CheckoutStatus.PAID);

        deliveryId = "DEL-" + id;
        status = CheckoutStatus.DISPATCHED;
    }

    private void require(CheckoutStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + ", got " + status
            );
        }
    }
}