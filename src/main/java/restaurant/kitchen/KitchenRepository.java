package restaurant.kitchen;

import restaurant.storage.JsonFiles;

import java.nio.file.Path;
import java.util.List;

public class KitchenRepository {
    private final Path file;

    public KitchenRepository(Path directory) {
        file = directory.resolve("kitchen.json");
    }

    public List<KitchenTask> findAll() {
        return JsonFiles.read(file, KitchenTask.class);
    }

    public KitchenTask find(String id) {
        for (KitchenTask item : findAll()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    public void save(KitchenTask item) {
        List<KitchenTask> items = findAll();

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