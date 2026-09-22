package com.naveen.healthcare.payersservice.exception;


public class PayerNotFoundException
        extends RuntimeException {


    public PayerNotFoundException(
            String message
    ) {

        super(message);
    }
}
