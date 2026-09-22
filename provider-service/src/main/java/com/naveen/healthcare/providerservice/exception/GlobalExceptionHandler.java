package com.naveen.healthcare.providerservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
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

    @ExceptionHandler(ProviderNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleProviderNotFound(
            ProviderNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Provider Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateProviderException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateProvider(
            DuplicateProviderException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Provider Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidProviderOperationException.class)
    public ResponseEntity<ErrorResponse> handleInvalidProviderOperation(
            InvalidProviderOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Provider Operation",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
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

        String message;

        if ("providerId".equals(exception.getName())) {
            message = "The supplied Provider ID has an invalid format";
        } else {
            message = "The supplied request parameter has an invalid format";
        }

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                message,
                request
        );
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Data Conflict",
                "A Provider organization with the supplied unique information already exists",
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

        ErrorResponse response = new ErrorResponse(
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

    @ExceptionHandler(DepartmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDepartmentNotFound(
            DepartmentNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Department Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateDepartmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDepartment(
            DuplicateDepartmentException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Department Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidDepartmentOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidDepartmentOperation(
            InvalidDepartmentOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Department Operation",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DoctorNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleDoctorNotFound(
            DoctorNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Doctor Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateDoctorException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateDoctor(
            DuplicateDoctorException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Doctor Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidDoctorOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidDoctorOperation(
            InvalidDoctorOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Doctor Operation",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DoctorAvailabilityNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleDoctorAvailabilityNotFound(
            DoctorAvailabilityNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Doctor Availability Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateDoctorAvailabilityException.class)
    public ResponseEntity<ErrorResponse>
    handleDuplicateDoctorAvailability(
            DuplicateDoctorAvailabilityException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Doctor Availability Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidDoctorAvailabilityException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidDoctorAvailability(
            InvalidDoctorAvailabilityException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Doctor Availability",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MedicalRecordNotFoundException.class)
    public ResponseEntity<ErrorResponse>
    handleMedicalRecordNotFound(
            MedicalRecordNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Medical Record Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateMedicalRecordException.class)
    public ResponseEntity<ErrorResponse>
    handleDuplicateMedicalRecord(
            DuplicateMedicalRecordException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Medical Record Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidMedicalRecordOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidMedicalRecordOperation(
            InvalidMedicalRecordOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Medical Record Operation",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(AppointmentServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse>
    handleAppointmentServiceCommunication(
            AppointmentServiceCommunicationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Appointment Service Unavailable",
                exception.getMessage(),
                request
        );
    }
}