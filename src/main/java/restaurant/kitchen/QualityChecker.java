package restaurant.kitchen;

public class QualityChecker {

    public String stationFor(String dish) {
        if ("Burger".equals(dish)) {
            return "ST-01";
        }

        if ("Pizza".equals(dish)) {
            return "ST-02";
        }

        if ("Pasta".equals(dish)) {
            return "ST-03";
        }

        throw new IllegalArgumentException("Unknown dish");
    }

    public void check(
            String dish,
            int quantity,
            String station,
            boolean busy
    ) {
        if (quantity <= 0 || !stationFor(dish).equals(station)) {
            throw new KitchenException("Station does not match the order");
        }

        if (busy) {
            throw new KitchenException("Suitable station is busy");
        }
    }
}