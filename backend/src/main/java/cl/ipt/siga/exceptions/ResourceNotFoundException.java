package cl.ipt.siga.exceptions;

/**
 * Thrown when a requested resource does not exist.
 * The global exception handler translates it to HTTP 404 (Not Found).
 */
public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }
}
