package Exceptions;

/**
 * A exceção InvalidOperationFormatException é uma exceção personalizada que 
 * indica que o formato de uma operação é inválido.
 * 
 * Esta exceção é lançada quando o formato de uma operação fornecida não corresponde 
 * ao esperado pelo sistema.
 */
public class InvalidOperationFormatException extends RuntimeException {
    public InvalidOperationFormatException(String message) {
        super(message);
    }
}
