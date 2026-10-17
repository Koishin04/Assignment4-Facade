package restaurant.facade;

import restaurant.order.*;
import restaurant.kitchen.*;
import restaurant.billing.*;
import restaurant.chain.*;

import java.nio.file.Path;
import java.util.UUID;

public class RestaurantFacade {
    private final OrderQueue orders;
    private final CookingStationAllocator kitchen;
    private final DeliveryDispatcher billing;
    private final ChainProgressRepository chains;

    public RestaurantFacade(Path directory) {
        orders = new OrderQueue(directory);
        kitchen = new CookingStationAllocator(directory);
        billing = new DeliveryDispatcher(directory);
        chains = new ChainProgressRepository(directory);
    }

    public ChainProgress start(
            String dish,
            int quantity,
            String address,
            boolean approved,
            int steps
    ) {
        if (steps < 1 || steps > 3) {
            throw new IllegalArgumentException("Steps must be 1, 2 or 3");
        }

        String id = UUID.randomUUID().toString();

        ChainProgress chain = new ChainProgress(
                id,
                dish,
                quantity,
                address,
                approved
        );

        chains.save(chain);

        return process(id, steps);
    }

    public ChainProgress resume(String id) {
        return process(id, 3);
    }

    public void recoverAll() {
        for (ChainProgress chain : chains.findAll()) {
            if (!chain.isFinished()) {
                resume(chain.getId());
            }
        }
    }

    private ChainProgress process(String id, int steps) {
        ChainProgress chain = require(id);

        if (chain.isFinished()) {
            return chain;
        }

        if (chain.getStatus() == ChainStatus.COMPENSATING_B
                || chain.getStatus() == ChainStatus.COMPENSATING_A) {
            return finishCompensation(chain);
        }

        Checkout checkout = billing.find(id);
        KitchenTask task = kitchen.find(id);
        Order order = orders.find(id);

        if (checkout != null
                && checkout.getStatus() == CheckoutStatus.FAILED) {
            return beginCompensation(chain, true, "C_FAILED");
        }

        if (task != null
                && task.getStatus() == KitchenStatus.REJECTED) {
            return beginCompensation(chain, false, "B_FAILED");
        }

        if ((task != null && task.getStatus() == KitchenStatus.CANCELLED)
                || (order != null && order.getStatus() == OrderStatus.CANCELLED)) {
            return beginCompensation(
                    chain,
                    task != null,
                    "CANCEL_REQUESTED"
            );
        }

        try {
            orders.reserve(
                    id,
                    chain.getDish(),
                    chain.getQuantity(),
                    chain.getAddress()
            );
        } catch (OrderException e) {
            save(
                    chain,
                    ChainStatus.REJECTED,
                    "A_REJECTED: " + e.getMessage()
            );
            return chain;
        }

        save(chain, ChainStatus.PENDING_B, "A_RESERVED");

        if (steps == 1) {
            return chain;
        }

        try {
            kitchen.prepare(
                    id,
                    chain.getDish(),
                    chain.getQuantity()
            );
        } catch (KitchenException e) {
            return beginCompensation(
                    chain,
                    false,
                    "B_FAILED: " + e.getMessage()
            );
        }

        save(chain, ChainStatus.PENDING_C, "B_READY");

        if (steps == 2) {
            return chain;
        }

        try {
            billing.complete(
                    id,
                    chain.getDish(),
                    chain.getQuantity(),
                    chain.getAddress(),
                    chain.isApproved()
            );
        } catch (PaymentException e) {
            return beginCompensation(
                    chain,
                    true,
                    "C_FAILED: " + e.getMessage()
            );
        }

        save(chain, ChainStatus.FINALIZING, "C_DISPATCHED");

        orders.confirm(id);
        kitchen.confirm(id);

        save(chain, ChainStatus.COMPLETED, "COMPLETED");

        return chain;
    }

    public ChainProgress cancel(String id) {
        ChainProgress chain = require(id);

        if (chain.getStatus() == ChainStatus.COMPENSATED
                || chain.getStatus() == ChainStatus.REJECTED) {
            return chain;
        }

        Checkout checkout = billing.find(id);

        if (checkout != null
                && (checkout.getStatus() == CheckoutStatus.PAID
                || checkout.getStatus() == CheckoutStatus.DISPATCHED)) {
            throw new IllegalStateException(
                    "Payment is already accepted: resume this order"
            );
        }

        if (chain.getStatus() == ChainStatus.COMPLETED) {
            throw new IllegalStateException(
                    "Completed orders cannot be compensated"
            );
        }

        if (chain.getStatus() == ChainStatus.COMPENSATING_B
                || chain.getStatus() == ChainStatus.COMPENSATING_A) {
            return finishCompensation(chain);
        }

        KitchenTask task = kitchen.find(id);

        boolean undoB = task != null
                && task.getStatus() != KitchenStatus.REJECTED;

        return beginCompensation(chain, undoB, "MANUAL_CANCEL");
    }

    private ChainProgress beginCompensation(
            ChainProgress chain,
            boolean undoB,
            String reason
    ) {
        if (undoB) {
            save(chain, ChainStatus.COMPENSATING_B, reason);
        } else {
            save(chain, ChainStatus.COMPENSATING_A, reason);
        }

        return finishCompensation(chain);
    }

    private ChainProgress finishCompensation(ChainProgress chain) {
        if (chain.getStatus() == ChainStatus.COMPENSATING_B) {
            kitchen.cancel(chain.getId());
            save(chain, ChainStatus.COMPENSATING_A, "B_CANCELLED");
        }

        if (chain.getStatus() == ChainStatus.COMPENSATING_A) {
            orders.cancel(chain.getId());
            save(chain, ChainStatus.COMPENSATED, "A_CANCELLED");
        }

        return chain;
    }

    private void save(
            ChainProgress chain,
            ChainStatus status,
            String event
    ) {
        chain.move(status, event);
        chains.save(chain);
    }

    private ChainProgress require(String id) {
        ChainProgress chain = chains.find(id);

        if (chain == null) {
            throw new IllegalArgumentException("Chain not found");
        }

        return chain;
    }
}