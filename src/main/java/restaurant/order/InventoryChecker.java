package restaurant.order;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class InventoryChecker {

    public Map<String, Long> initialStock() {
        Map<String, Long> stock = new LinkedHashMap<>();

        stock.put("bun", 12L);
        stock.put("patty", 12L);
        stock.put("cheese", 30L);
        stock.put("dough", 10L);
        stock.put("tomato", 30L);
        stock.put("pasta", 10L);

        return stock;
    }

    public Map<String, Long> recipe(String dish, int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }

        long count = quantity;
        Map<String, Long> result = new LinkedHashMap<>();

        if ("Burger".equals(dish)) {
            result.put("bun", count);
            result.put("patty", count);
            result.put("cheese", count);
        } else if ("Pizza".equals(dish)) {
            result.put("dough", count);
            result.put("tomato", count * 2);
            result.put("cheese", count * 2);
        } else if ("Pasta".equals(dish)) {
            result.put("pasta", count);
            result.put("tomato", count);
            result.put("cheese", count);
        } else {
            throw new IllegalArgumentException("Unknown dish");
        }

        return result;
    }

    public Map<String, Long> available(List<Order> orders) {
        Map<String, Long> stock = initialStock();

        for (Order order : orders) {
            if (order.getStatus() == OrderStatus.RESERVED
                    || order.getStatus() == OrderStatus.CONFIRMED) {

                for (Map.Entry<String, Long> item
                        : order.getReservation().entrySet()) {

                    long remaining = stock.get(item.getKey())
                            - item.getValue();

                    stock.put(item.getKey(), remaining);
                }
            }
        }

        return stock;
    }

    public boolean enough(
            Map<String, Long> stock,
            Map<String, Long> needed
    ) {
        for (Map.Entry<String, Long> item : needed.entrySet()) {
            if (stock.getOrDefault(item.getKey(), 0L) < item.getValue()) {
                return false;
            }
        }

        return true;
    }
}