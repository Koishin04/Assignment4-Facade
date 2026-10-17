package restaurant.billing;

import java.nio.file.Path;
import java.util.List;

public class DeliveryDispatcher {
    private final CheckoutRepository repository;
    private final BillingModule billing = new BillingModule();

    public DeliveryDispatcher(Path directory) {
        repository = new CheckoutRepository(directory);
    }

    public Checkout complete(
            String id,
            String dish,
            int quantity,
            String address,
            boolean approved
    ) {
        long total = billing.total(dish, quantity);
        Checkout checkout = repository.find(id);

        if (checkout == null) {
            checkout = new Checkout(id, total, address);
            repository.save(checkout);
        } else if (checkout.getTotal() != total
                || !checkout.getAddress().equals(address.trim())) {
            throw new IllegalArgumentException(
                    "Checkout ID belongs to another request"
            );
        }

        if (checkout.getStatus() == CheckoutStatus.NEW
                || checkout.getStatus() == CheckoutStatus.FAILED) {
            try {
                billing.charge(checkout, approved);
            } catch (PaymentException e) {
                repository.save(checkout);
                throw e;
            }

            repository.save(checkout);
        }

        checkout.dispatch();
        repository.save(checkout);

        return checkout;
    }

    public Checkout find(String id) {
        return repository.find(id);
    }

    public List<Checkout> all() {
        return repository.findAll();
    }
}