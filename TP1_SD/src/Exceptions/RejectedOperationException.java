package Exceptions;

/**
 * A exceção RejectedOperationException é uma exceção personalizada que
 * indica que uma operação foi rejeitada.
 * 
 * Esta exceção é lançada quando uma operação não pode ser concluída devido a
 * uma
 * rejeição explícita, seja por falha de validação, falta de permissões ou
 * outros motivos.
 */
public class RejectedOperationException extends RuntimeException {
    public RejectedOperationException(String message) {
        super(message);
    }
}
