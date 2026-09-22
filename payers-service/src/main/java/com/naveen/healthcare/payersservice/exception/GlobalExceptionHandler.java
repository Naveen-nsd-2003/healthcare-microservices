/*
package com.naveen.healthcare.payersservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.stream.Collectors;


@RestControllerAdvice
public class GlobalExceptionHandler {


    // =====================================================
    // PAYER NOT FOUND
    // =====================================================

    @ExceptionHandler(PayerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePayerNotFound(
            PayerNotFoundException exception,
            HttpServletRequest request
    ) {


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.NOT_FOUND.value(),

                "Payer Not Found",

                exception.getMessage(),

                request.getRequestURI()
        );


        return ResponseEntity

                .status(HttpStatus.NOT_FOUND)

                .body(response);
    }


    // =====================================================
    // DUPLICATE PAYER
    // =====================================================

    @ExceptionHandler(DuplicatePayerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayer(
            DuplicatePayerException exception,
            HttpServletRequest request
    ) {


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.CONFLICT.value(),

                "Conflict",

                exception.getMessage(),

                request.getRequestURI()
        );


        return ResponseEntity

                .status(HttpStatus.CONFLICT)

                .body(response);
    }


    // =====================================================
    // VALIDATION FAILURE
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {


        String message = exception

                .getBindingResult()

                .getFieldErrors()

                .stream()

                .map(error ->

                        error.getField()

                                + ": "

                                + error.getDefaultMessage()
                )

                .collect(Collectors.joining(", "));


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.BAD_REQUEST.value(),

                "Validation Failed",

                message,

                request.getRequestURI()
        );


        return ResponseEntity

                .badRequest()

                .body(response);
    }


    // =====================================================
    // INVALID REQUEST BODY OR ENUM
    // =====================================================

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleUnreadableRequest(
            HttpMessageNotReadableException exception,
            HttpServletRequest request
    ) {


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.BAD_REQUEST.value(),

                "Invalid Request",

                "Request body contains an invalid or unsupported value",

                request.getRequestURI()
        );


        return ResponseEntity

                .badRequest()

                .body(response);
    }


    // =====================================================
    // DATABASE CONSTRAINT VIOLATION
    // =====================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.CONFLICT.value(),

                "Data Conflict",

                "A payer organization with the supplied unique information already exists",

                request.getRequestURI()
        );


        return ResponseEntity

                .status(HttpStatus.CONFLICT)

                .body(response);
    }


    // =====================================================
    // GENERAL EXCEPTION
    // =====================================================

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneralException(
            Exception exception,
            HttpServletRequest request
    ) {


        ErrorResponse response = new ErrorResponse(

                LocalDateTime.now(),

                HttpStatus.INTERNAL_SERVER_ERROR.value(),

                "Internal Server Error",

                "An unexpected error occurred",

                request.getRequestURI()
        );


        return ResponseEntity

                .status(HttpStatus.INTERNAL_SERVER_ERROR)

                .body(response);
    }
}*/


package com.naveen.healthcare.payersservice.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.security.access.AccessDeniedException;
import java.time.LocalDateTime;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =====================================================
    // PAYER NOT FOUND
    // =====================================================

    @ExceptionHandler(PayerNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePayerNotFound(
            PayerNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Payer Not Found",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // DUPLICATE PAYER
    // =====================================================

    @ExceptionHandler(DuplicatePayerException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePayer(
            DuplicatePayerException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Payer Conflict",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // INSURANCE PLAN NOT FOUND
    // =====================================================

    @ExceptionHandler(InsurancePlanNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleInsurancePlanNotFound(
            InsurancePlanNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Insurance Plan Not Found",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // DUPLICATE INSURANCE PLAN
    // =====================================================

    @ExceptionHandler(DuplicateInsurancePlanException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateInsurancePlan(
            DuplicateInsurancePlanException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Insurance Plan Conflict",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // INVALID PAYER STATUS
    // =====================================================

    @ExceptionHandler(InvalidPayerStatusException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPayerStatus(
            InvalidPayerStatusException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Payer Organization Is Not Active",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // INVALID INSURANCE PLAN BUSINESS DATA
    // =====================================================

    @ExceptionHandler(InvalidInsurancePlanDataException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidInsurancePlanData(
            InvalidInsurancePlanDataException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Insurance Plan Data",
                exception.getMessage(),
                request
        );
    }

    // =====================================================
    // REQUEST BODY VALIDATION FAILURE
    // =====================================================

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationException(
            MethodArgumentNotValidException exception,
            HttpServletRequest request
    ) {

        String message = exception
                .getBindingResult()
                .getFieldErrors()
                .stream()
                .map(error ->
                        error.getField()
                                + ": "
                                + error.getDefaultMessage()
                )
                .collect(Collectors.joining(", "));

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Validation Failed",
                message,
                request
        );
    }

    // =====================================================
    // INVALID REQUEST BODY OR ENUM VALUE
    // =====================================================

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

    // =====================================================
    // INVALID PATH VARIABLE OR REQUEST PARAMETER
    // =====================================================

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Parameter",
                "The supplied identifier or parameter has an invalid format",
                request
        );
    }

    // =====================================================
    // DATABASE CONSTRAINT VIOLATION
    // =====================================================

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse>
    handleDataIntegrityViolation(
            DataIntegrityViolationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Data Conflict",
                "A record with the supplied unique information already exists",
                request
        );
    }

    // =====================================================
    // GENERAL UNEXPECTED EXCEPTION
    // =====================================================

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

    // =====================================================
    // COMMON ERROR RESPONSE BUILDER
    // =====================================================

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

    @ExceptionHandler(MemberVerificationException.class)
    public ResponseEntity<ErrorResponse> handleMemberVerification(
            MemberVerificationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Member Verification Failed",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(MemberServiceCommunicationException.class)
    public ResponseEntity<ErrorResponse> handleMemberServiceCommunication(
            MemberServiceCommunicationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "Member Service Unavailable",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(EnrollmentNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleEnrollmentNotFound(
            EnrollmentNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Enrollment Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicateEnrollmentException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateEnrollment(
            DuplicateEnrollmentException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Enrollment Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidEnrollmentOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidEnrollmentOperation(
            InvalidEnrollmentOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Enrollment Operation",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(PolicyNotFoundException.class)
    public ResponseEntity<ErrorResponse> handlePolicyNotFound(
            PolicyNotFoundException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.NOT_FOUND,
                "Policy Not Found",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(DuplicatePolicyException.class)
    public ResponseEntity<ErrorResponse> handleDuplicatePolicy(
            DuplicatePolicyException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.CONFLICT,
                "Policy Conflict",
                exception.getMessage(),
                request
        );
    }

    @ExceptionHandler(InvalidPolicyOperationException.class)
    public ResponseEntity<ErrorResponse>
    handleInvalidPolicyOperation(
            InvalidPolicyOperationException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Policy Operation",
                exception.getMessage(),
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

    @ExceptionHandler(InvalidPolicyClaimException.class)
    public ResponseEntity<ErrorResponse> handleInvalidPolicyClaim(
            InvalidPolicyClaimException exception,
            HttpServletRequest request
    ) {

        return buildErrorResponse(
                HttpStatus.BAD_REQUEST,
                "Policy Claim Verification Failed",
                exception.getMessage(),
                request
        );
    }

}