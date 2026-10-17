package restaurant.billing;

import restaurant.storage.JsonFiles;

import java.nio.file.Path;
import java.util.List;

public class CheckoutRepository {
    private final Path file;

    public CheckoutRepository(Path directory) {
        file = directory.resolve("checkouts.json");
    }

    public List<Checkout> findAll() {
        return JsonFiles.read(file, Checkout.class);
    }

    public Checkout find(String id) {
        for (Checkout item : findAll()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    public void save(Checkout item) {
        List<Checkout> items = findAll();

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