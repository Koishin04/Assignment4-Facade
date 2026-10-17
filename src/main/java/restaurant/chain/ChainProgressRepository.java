package restaurant.chain;

import restaurant.storage.JsonFiles;

import java.nio.file.Path;
import java.util.List;

public class ChainProgressRepository {
    private final Path file;

    public ChainProgressRepository(Path directory) {
        file = directory.resolve("chains.json");
    }

    public List<ChainProgress> findAll() {
        return JsonFiles.read(file, ChainProgress.class);
    }

    public ChainProgress find(String id) {
        for (ChainProgress item : findAll()) {
            if (item.getId().equals(id)) {
                return item;
            }
        }

        return null;
    }

    public void save(ChainProgress item) {
        List<ChainProgress> items = findAll();

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