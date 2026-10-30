package cl.ipt.siga.exceptions;

import cl.ipt.siga.dtos.error_dto.ApiErrorResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.stream.Collectors;

/**
 * Centralized exception handling for the whole API.
 *
 * Controllers do not need try/catch blocks: every exception is translated
 * here into a consistent ApiErrorResponse with the proper HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /** Resource not found -> 404. */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    /** Business rule violated -> 409. */
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ApiErrorResponse> handleBusiness(BusinessException ex,
                                                           HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    /** Bean Validation errors on request DTOs (@NotBlank, @Size, ...) -> 400. */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                             HttpServletRequest request) {
        String message = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error.getDefaultMessage())
                .collect(Collectors.joining("; "));

        if (message.isBlank()) {
            message = "Validation failed";
        }
        return build(HttpStatus.BAD_REQUEST, message, request);
    }

    /** Database constraint violated (for example a UNIQUE constraint) -> 409. */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                                HttpServletRequest request) {
        return build(HttpStatus.CONFLICT,
                "The operation violates a database constraint", request);
    }

    /** Malformed JSON or an invalid enum value in the body -> 400. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiErrorResponse> handleUnreadable(HttpMessageNotReadableException ex,
                                                             HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    /** Invalid type in a path or query parameter -> 400. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ApiErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.BAD_REQUEST,
                "Invalid value for parameter: " + ex.getName(), request);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status,
                                                   String message,
                                                   HttpServletRequest request) {
        ApiErrorResponse body = new ApiErrorResponse(
                LocalDateTime.now(),
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }
}
