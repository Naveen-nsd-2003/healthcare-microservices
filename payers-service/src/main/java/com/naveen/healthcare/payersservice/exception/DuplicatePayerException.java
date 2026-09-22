package com.naveen.healthcare.payersservice.exception;


public class DuplicatePayerException
        extends RuntimeException {


    public DuplicatePayerException(
            String message
    ) {

        super(message);
    }
}