package com.naveen.healthcare.payersservice.exception;

public class InsurancePlanNotFoundException
        extends RuntimeException {

    public InsurancePlanNotFoundException(
            String message
    ) {

        super(message);
    }
}
