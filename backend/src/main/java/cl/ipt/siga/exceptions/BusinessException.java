package cl.ipt.siga.exceptions;

/**
 * Thrown when an operation violates a business rule.
 * The global exception handler translates it to HTTP 409 (Conflict).
 */
public class BusinessException extends RuntimeException {

    public BusinessException(String message) {
        super(message);
    }
}
