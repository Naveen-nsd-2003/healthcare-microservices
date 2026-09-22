package com.naveen.healthcare.notificationservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(
            NotificationNotFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleNotificationNotFound(
            NotificationNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Notification Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(
            IllegalArgumentException.class
    )
    public ResponseEntity<ErrorResponse>
    handleIllegalArgument(
            IllegalArgumentException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Notification Request",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(
            MethodArgumentTypeMismatchException.class
    )
    public ResponseEntity<ErrorResponse>
    handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                "The request contains a parameter "
                        + "with an invalid format",
                request
        );
    }

    @ExceptionHandler(
            AccessDeniedException.class
    )
    public ResponseEntity<ErrorResponse>
    handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission "
                        + "to access this resource",
                request
        );
    }

    @ExceptionHandler(
            NoResourceFoundException.class
    )
    public ResponseEntity<ErrorResponse>
    handleNoResourceFound(
            NoResourceFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Endpoint Not Found",
                "The requested endpoint does not exist",
                request
        );
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse>
    handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "An unexpected error occurred",
                request
        );
    }

    private ResponseEntity<ErrorResponse>
    buildErrorResponse(
            HttpStatus status,
            String error,
            String message,
            HttpServletRequest request
    ) {

        ErrorResponse response =
                new ErrorResponse(
                        LocalDateTime.now(),
                        status.value(),
                        error,
                        message,
                        request.getRequestURI()
                );

        return ResponseEntity
                .status(status)
                .body(response);
    }
}