package restaurant.billing;

public class PaymentException extends RuntimeException {
    public PaymentException(String message) {
        super(message);
    }
}