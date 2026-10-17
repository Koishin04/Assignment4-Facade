package restaurant.chain;

import java.util.ArrayList;
import java.util.List;

public class ChainProgress {
    private String id;
    private String dish;
    private int quantity;
    private String address;
    private boolean approved;

    private ChainStatus status = ChainStatus.CREATED;
    private List<String> events = new ArrayList<>();

    public ChainProgress() {
    }

    public ChainProgress(
            String id,
            String dish,
            int quantity,
            String address,
            boolean approved
    ) {
        if (id == null || id.isBlank()
                || quantity <= 0
                || address == null || address.isBlank()) {
            throw new IllegalArgumentException("Invalid order input");
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
        this.approved = approved;
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

    public boolean isApproved() {
        return approved;
    }

    public ChainStatus getStatus() {
        return status;
    }

    public List<String> getEvents() {
        return new ArrayList<>(events);
    }

    public void move(ChainStatus next, String event) {
        status = next;

        if (!events.contains(event)) {
            events.add(event);
        }
    }

    public boolean isFinished() {
        return status == ChainStatus.COMPLETED
                || status == ChainStatus.COMPENSATED
                || status == ChainStatus.REJECTED;
    }
}