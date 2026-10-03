
package com.jobmatcher.exception;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataAccessException;
import org.springframework.web.ErrorResponse;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

        private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // DTO validation errors
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request) {

        Map<String, String> errors = new LinkedHashMap<>();

        exception.getBindingResult()
                .getFieldErrors()
                .forEach(error ->
                        errors.put(
                                error.getField(),
                                error.getDefaultMessage()
                        )
                );

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                errors,
                request.getRequestURI()
        );
    }

    // Illegal arguments
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(
            IllegalArgumentException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    // Constraint validation errors
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(
            ConstraintViolationException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Validation failed",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    // Wrong email/password
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<Map<String, Object>> handleBadCredentials(
            BadCredentialsException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                "Invalid email or password",
                request.getRequestURI()
        );
    }

    // Other authentication errors
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<Map<String, Object>> handleAuthenticationException(
            AuthenticationException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.UNAUTHORIZED,
                "Authentication failed",
                "Authentication failed",
                request.getRequestURI()
        );
    }

    // Permission errors
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, Object>> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.FORBIDDEN,
                "Access denied",
                "You do not have permission to access this resource",
                request.getRequestURI()
        );
    }

    // Upload bigger than the allowed size
@ExceptionHandler(MaxUploadSizeExceededException.class)
public ResponseEntity<Map<String, Object>> handleMaxUpload(
        MaxUploadSizeExceededException exception,
        HttpServletRequest request) {

    return buildResponse(HttpStatus.PAYLOAD_TOO_LARGE, "File too large",
            "The file is too large. Maximum size is 5 MB.", request.getRequestURI());
}

// Database errors: log the details, show a generic message
@ExceptionHandler(DataAccessException.class)
public ResponseEntity<Map<String, Object>> handleDatabaseError(
        DataAccessException exception,
        HttpServletRequest request) {

    log.error("Database error on {}", request.getRequestURI(), exception);

    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server error",
            "A database error occurred. Please try again.", request.getRequestURI());
}

// Anything else we did not expect
@ExceptionHandler(Exception.class)
public ResponseEntity<Map<String, Object>> handleUnexpected(
        Exception exception,
        HttpServletRequest request) {

    // Spring's own errors (404, 405...) keep their correct status code.
    if (exception instanceof ErrorResponse errorResponse) {
        HttpStatus status = HttpStatus.valueOf(errorResponse.getStatusCode().value());
        return buildResponse(status, status.getReasonPhrase(),
                status.getReasonPhrase(), request.getRequestURI());
    }

    log.error("Unexpected error on {}", request.getRequestURI(), exception);

    return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Server error",
            "Something went wrong. Please try again later.", request.getRequestURI());
}

    // Other unexpected runtime errors
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntimeException(
            RuntimeException exception,
            HttpServletRequest request) {

        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Bad request",
                exception.getMessage(),
                request.getRequestURI()
        );
    }

    private ResponseEntity<Map<String, Object>> buildResponse(
            HttpStatus status,
            String error,
            Object message,
            String path) {

        Map<String, Object> response = new LinkedHashMap<>();

        response.put("timestamp", LocalDateTime.now());
        response.put("status", status.value());
        response.put("error", error);
        response.put("message", message);
        response.put("path", path);

        return ResponseEntity
                .status(status)
                .body(response);
    }
}

