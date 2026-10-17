package restaurant.order;

import java.util.LinkedHashMap;
import java.util.Map;

public class Order {
    private String id;
    private String dish;
    private int quantity;
    private String address;
    private OrderStatus status = OrderStatus.NEW;

    private Map<String, Long> reservation = new LinkedHashMap<>();

    public Order() {
    }

    public Order(String id, String dish, int quantity, String address) {
        if (id == null || id.isBlank()
                || address == null || address.isBlank()) {
            throw new IllegalArgumentException("ID and address are required");
        }

        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        if (!"Burger".equals(dish)
                && !"Pizza".equals(dish)
                && !"Pasta".equals(dish)) {
            throw new IllegalArgumentException("Unknown dish");
        }

        this.id = id;
        this.dish = dish;
        this.quantity = quantity;
        this.address = address.trim();
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

    public String getAddress() {
        return address;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public Map<String, Long> getReservation() {
        return new LinkedHashMap<>(reservation);
    }

    public void reserve(Map<String, Long> ingredients) {
        if (status == OrderStatus.RESERVED) {
            return;
        }

        require(OrderStatus.NEW);

        reservation = new LinkedHashMap<>(ingredients);
        status = OrderStatus.RESERVED;
    }

    public void confirm() {
        if (status == OrderStatus.CONFIRMED) {
            return;
        }

        require(OrderStatus.RESERVED);
        status = OrderStatus.CONFIRMED;
    }

    public void cancel() {
        if (status == OrderStatus.CANCELLED) {
            return;
        }

        if (status != OrderStatus.NEW && status != OrderStatus.RESERVED) {
            throw new IllegalStateException("Cannot cancel " + status);
        }

        status = OrderStatus.CANCELLED;
    }

    public void reject() {
        require(OrderStatus.NEW);
        status = OrderStatus.REJECTED;
    }

    private void require(OrderStatus expected) {
        if (status != expected) {
            throw new IllegalStateException(
                    "Expected " + expected + ", got " + status
            );
        }
    }
}