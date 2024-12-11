package Exceptions;

/**
 * A exceção InvalidUserException é uma exceção personalizada que 
 * indica que um utilizador não é válido para uma determinada operação ou contexto.
 * 
 * Esta exceção é lançada quando um utilizador não possui as permissões ou os 
 * atributos necessários para realizar uma ação específica no sistema.
 */
public class InvalidUserException extends RuntimeException {
    public InvalidUserException(String message) {
        super(message);
    }
}
