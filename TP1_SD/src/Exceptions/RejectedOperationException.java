package Exceptions;

public class RejectedOperationException extends RuntimeException {
    public RejectedOperationException(String message) {
        super(message);
    }
}
