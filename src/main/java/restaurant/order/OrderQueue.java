package restaurant.order;

import java.nio.file.Path;
import java.util.List;
import java.util.Map;

public class OrderQueue {
    private final OrderRepository repository;
    private final InventoryChecker inventory = new InventoryChecker();

    public OrderQueue(Path directory) {
        repository = new OrderRepository(directory);
    }

    public Order reserve(
            String id,
            String dish,
            int quantity,
            String address
    ) {
        Order order = repository.find(id);

        if (order == null) {
            order = new Order(id, dish, quantity, address);
        } else if (!order.getDish().equals(dish)
                || order.getQuantity() != quantity
                || !order.getAddress().equals(address.trim())) {
            throw new IllegalArgumentException(
                    "Order ID belongs to another request"
            );
        }

        if (order.getStatus() == OrderStatus.RESERVED
                || order.getStatus() == OrderStatus.CONFIRMED) {
            return order;
        }

        if (order.getStatus() != OrderStatus.NEW) {
            throw new OrderException("Order is " + order.getStatus());
        }

        Map<String, Long> needed = inventory.recipe(dish, quantity);

        if (!inventory.enough(stock(), needed)) {
            order.reject();
            repository.save(order);
            throw new OrderException("Not enough ingredients");
        }

        order.reserve(needed);
        repository.save(order);

        return order;
    }

    public void confirm(String id) {
        Order order = require(id);
        order.confirm();
        repository.save(order);
    }

    public void cancel(String id) {
        Order order = repository.find(id);

        if (order == null || order.getStatus() == OrderStatus.REJECTED) {
            return;
        }

        order.cancel();
        repository.save(order);
    }

    public Order find(String id) {
        return repository.find(id);
    }

    public List<Order> all() {
        return repository.findAll();
    }

    public Map<String, Long> stock() {
        return inventory.available(repository.findAll());
    }

    private Order require(String id) {
        Order order = repository.find(id);

        if (order == null) {
            throw new IllegalArgumentException("Order not found");
        }

        return order;
    }
}