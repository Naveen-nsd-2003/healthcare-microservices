package com.naveen.healthcare.payersservice.exception;

public class MemberVerificationException
        extends RuntimeException {

    public MemberVerificationException(
            String message
    ) {
        super(message);
    }
}
