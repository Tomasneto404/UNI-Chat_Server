package Exceptions;

public class StopReadingException extends RuntimeException {
    public StopReadingException(String message) {
        super(message);
    }
}
