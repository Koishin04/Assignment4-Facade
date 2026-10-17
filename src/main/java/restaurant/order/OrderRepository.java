package restaurant.order;

import restaurant.storage.JsonFiles;

import java.nio.file.Path;
import java.util.List;

public class OrderRepository {
    private final Path file;

    public OrderRepository(Path directory) {
        file = directory.resolve("orders.json");
    }

    public List<Order> findAll() {
        return JsonFiles.read(file, Order.class);
    }

    public Order find(String id) {
        for (Order item : findAll()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    public void save(Order item) {
        List<Order> items = findAll();

        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).getId().equals(item.getId())) {
                items.set(i, item);
                JsonFiles.write(file, items);
                return;
            }
        }

        items.add(item);
        JsonFiles.write(file, items);
    }
}