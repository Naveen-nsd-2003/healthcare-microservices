package com.naveen.healthcare.payersservice.exception;

public class DuplicateInsurancePlanException
        extends RuntimeException {

    public DuplicateInsurancePlanException(
            String message
    ) {

        super(message);
    }
}
