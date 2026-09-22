package com.naveen.healthcare.claimservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.LocalDateTime;
import java.util.Objects;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ClaimAppointmentVerificationException.class)
    public ResponseEntity<ErrorResponse>
    handleClaimAppointmentVerification(
            ClaimAppointmentVerificationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Appointment Verification Failed",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ClaimMedicalRecordVerificationException.class)
    public ResponseEntity<ErrorResponse>
    handleClaimMedicalRecordVerification(
            ClaimMedicalRecordVerificationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Medical Record Verification Failed",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(ClaimPolicyVerificationException.class)
    public ResponseEntity<ErrorResponse>
    handleClaimPolicyVerification(
            ClaimPolicyVerificationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Policy Verification Failed",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(RemoteServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse>
    handleRemoteServiceCommunication(
            RemoteServiceCommunicationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Remote Service Unavailable",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationFailure(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(fieldError ->
                        fieldError.getField()
                                + ": "
                                + Objects.requireNonNullElse(
                                fieldError.getDefaultMessage(),
                                "Invalid value"
                        )
                )
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                message,
                request
        );
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Request",
                "Request body contains an invalid or unsupported value",
                request
        );
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                "The request contains a parameter with an invalid format",
                request
        );
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(
            AccessDeniedException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.FORBIDDEN,
                "Forbidden",
                "You do not have permission to access this resource",
                request
        );
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoResourceFound(
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
    public ResponseEntity<ErrorResponse> handleGeneralException(
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

    private ResponseEntity<ErrorResponse> buildErrorResponse(
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

    @ExceptionHandler(ClaimNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleClaimNotFound(
            ClaimNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Claim Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateClaimException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateClaim(
            DuplicateClaimException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Claim Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidClaimOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidClaimOperation(
            InvalidClaimOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Claim Operation",
                exception.getMessage(),
                request
        );
    }

}