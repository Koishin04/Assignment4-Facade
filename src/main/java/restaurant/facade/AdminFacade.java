package restaurant.facade;

import restaurant.order.*;
import restaurant.kitchen.*;
import restaurant.billing.*;
import restaurant.chain.*;

import java.nio.file.Path;
import java.util.UUID;

public class AdminFacade {
    private final OrderQueue orders;
    private final CookingStationAllocator kitchen;
    private final DeliveryDispatcher billing;
    private final ChainProgressRepository chains;
    private final RestaurantFacade restaurant;

    public AdminFacade(Path directory) {
        orders = new OrderQueue(directory);
        kitchen = new CookingStationAllocator(directory);
        billing = new DeliveryDispatcher(directory);
        chains = new ChainProgressRepository(directory);
        restaurant = new RestaurantFacade(directory);
    }

    public void occupyAllStations() {
        for (String dish : new String[]{"Burger", "Pizza", "Pasta"}) {
            if (!kitchen.isBusy(dish)) {
                kitchen.prepare(
                        "DEMO-" + UUID.randomUUID(),
                        dish,
                        1
                );
            }
        }
    }

    public void releaseDemoStations() {
        for (KitchenTask task : kitchen.all()) {
            if (task.getId().startsWith("DEMO-")
                    && task.getStatus() == KitchenStatus.READY) {
                kitchen.cancel(task.getId());
            }
        }
    }

    public ChainProgress cancel(String id) {
        return restaurant.cancel(id);
    }

    public void finishKitchen(String id) {
        kitchen.finish(id);
    }

    public String inspect() {
        StringBuilder text = new StringBuilder();

        text.append("STOCK: ")
                .append(orders.stock())
                .append("\n");

        for (String dish : new String[]{"Burger", "Pizza", "Pasta"}) {
            text.append(dish)
                    .append(" station busy: ")
                    .append(kitchen.isBusy(dish))
                    .append("\n");
        }

        for (Order order : orders.all()) {
            text.append("ORDER ")
                    .append(order.getId())
                    .append(" ")
                    .append(order.getDish())
                    .append(" x ")
                    .append(order.getQuantity())
                    .append(" ")
                    .append(order.getStatus())
                    .append("\n");
        }

        for (KitchenTask task : kitchen.all()) {
            text.append("KITCHEN ")
                    .append(task.getId())
                    .append(" ")
                    .append(task.getStation())
                    .append(" ")
                    .append(task.getStatus())
                    .append("\n");
        }

        for (Checkout checkout : billing.all()) {
            text.append("CHECKOUT ")
                    .append(checkout.getId())
                    .append(" ")
                    .append(checkout.getTotal())
                    .append(" KZT ")
                    .append(checkout.getStatus())
                    .append(" ")
                    .append(checkout.getDeliveryId())
                    .append("\n");
        }

        for (ChainProgress chain : chains.findAll()) {
            text.append("CHAIN ")
                    .append(chain.getId())
                    .append(" ")
                    .append(chain.getStatus())
                    .append("\n")
                    .append(chain.getEvents())
                    .append("\n");
        }

        return text.toString();
    }
}