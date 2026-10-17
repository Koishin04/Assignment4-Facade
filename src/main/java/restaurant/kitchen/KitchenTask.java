package restaurant.kitchen;

public class KitchenTask {
    private String id;
    private String dish;
    private int quantity;
    private String station;
    private KitchenStatus status = KitchenStatus.REQUESTED;

    public KitchenTask() {
    }

    public KitchenTask(
            String id,
            String dish,
            int quantity,
            String station
    ) {
        if (id == null || id.isBlank() || quantity <= 0) {
            throw new IllegalArgumentException("Invalid kitchen task");
        }

        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
        this.station = station;
    }

    public String getId() {
        return id;
    }

    public String getDish() {
        return dish;
    }

    public int getQuantity() {
        return quantity;
    }

    public String getStation() {
        return station;
    }

    public KitchenStatus getStatus() {
        return status;
    }

    public void ready() {
        require(KitchenStatus.REQUESTED);
        status = KitchenStatus.READY;
    }

    public void reject() {
        require(KitchenStatus.REQUESTED);
        status = KitchenStatus.REJECTED;
    }

    public void confirm() {
        if (status == KitchenStatus.CONFIRMED
                || status == KitchenStatus.FINISHED) {
            return;
        }

        require(KitchenStatus.READY);
        status = KitchenStatus.CONFIRMED;
    }

    public void finish() {
        if (status == KitchenStatus.FINISHED) {
            return;
        }

        require(KitchenStatus.CONFIRMED);
        status = KitchenStatus.FINISHED;
    }

    public void cancel() {
        if (status == KitchenStatus.CANCELLED) {
            return;
        }

        if (status != KitchenStatus.REQUESTED
                && status != KitchenStatus.READY) {
            throw new IllegalStateException("Cannot cancel " + status);
        }

        status = KitchenStatus.CANCELLED;
    }

    public boolean occupiesStation() {
        return status == KitchenStatus.READY
                || status == KitchenStatus.CONFIRMED;
    }

    private void require(KitchenStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + ", got " + status
            );
        }
    }
}