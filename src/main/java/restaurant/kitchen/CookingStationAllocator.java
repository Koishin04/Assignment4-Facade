package restaurant.kitchen;

import java.nio.file.Path;
import java.util.List;

public class CookingStationAllocator {
    private final KitchenRepository repository;
    private final QualityChecker quality = new QualityChecker();

    public CookingStationAllocator(Path directory) {
        repository = new KitchenRepository(directory);
    }

    public KitchenTask prepare(String id, String dish, int quantity) {
        KitchenTask task = repository.find(id);

        if (task != null) {
            if (!task.getDish().equals(dish)
                    || task.getQuantity() != quantity) {
                throw new IllegalArgumentException(
                        "Task ID belongs to another request"
                );
            }

            if (task.getStatus() == KitchenStatus.REJECTED
                    || task.getStatus() == KitchenStatus.CANCELLED) {
                throw new KitchenException("Task is " + task.getStatus());
            }

            if (task.getStatus() != KitchenStatus.REQUESTED) {
                return task;
            }
        } else {
            task = new KitchenTask(
                    id,
                    dish,
                    quantity,
                    quality.stationFor(dish)
            );
        }

        try {
            quality.check(
                    dish,
                    quantity,
                    task.getStation(),
                    isBusy(dish)
            );
        } catch (KitchenException e) {
            task.reject();
            repository.save(task);
            throw e;
        }

        task.ready();
        repository.save(task);

        return task;
    }

    public boolean isBusy(String dish) {
        String station = quality.stationFor(dish);

        for (KitchenTask task : repository.findAll()) {
            if (station.equals(task.getStation())
                    && task.occupiesStation()) {
                return true;
            }
        }

        return false;
    }

    public void confirm(String id) {
        KitchenTask task = require(id);
        task.confirm();
        repository.save(task);
    }

    public void cancel(String id) {
        KitchenTask task = repository.find(id);

        if (task == null || task.getStatus() == KitchenStatus.REJECTED) {
            return;
        }

        task.cancel();
        repository.save(task);
    }

    public void finish(String id) {
        KitchenTask task = require(id);
        task.finish();
        repository.save(task);
    }

    public KitchenTask find(String id) {
        return repository.find(id);
    }

    public List<KitchenTask> all() {
        return repository.findAll();
    }

    private KitchenTask require(String id) {
        KitchenTask task = repository.find(id);

        if (task == null) {
            throw new IllegalArgumentException("Kitchen task not found");
        }

        return task;
    }
}