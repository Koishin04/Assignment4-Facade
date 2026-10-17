package restaurant.chain;

public enum ChainStatus {
    CREATED,
    PENDING_B,
    PENDING_C,
    FINALIZING,
    COMPENSATING_B,
    COMPENSATING_A,
    COMPENSATED,
    COMPLETED,
    REJECTED
}