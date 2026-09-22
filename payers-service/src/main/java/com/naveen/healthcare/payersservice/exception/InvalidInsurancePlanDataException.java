package com.naveen.healthcare.payersservice.exception;

public class InvalidInsurancePlanDataException
        extends RuntimeException {

    public InvalidInsurancePlanDataException(
            String message
    ) {

        super(message);
    }
}
