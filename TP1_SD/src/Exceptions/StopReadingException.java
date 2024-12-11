package Exceptions;

/**
 * A exceção StopReadingException é uma exceção personalizada que 
 * indica que o processo de leitura deve ser interrompido.
 * 
 * Esta exceção é útil em cenários onde é necessário parar a leitura de dados 
 * devido a condições específicas, como erros, limites ou outras restrições.
 */
public class StopReadingException extends RuntimeException {
    public StopReadingException(String message) {
        super(message);
    }
}
