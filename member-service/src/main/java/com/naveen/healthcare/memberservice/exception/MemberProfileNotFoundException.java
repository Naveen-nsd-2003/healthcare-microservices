package com.naveen.healthcare.memberservice.exception;

public class MemberProfileNotFoundException
        extends RuntimeException {

    public MemberProfileNotFoundException(
            String message
    ) {
        super(message);
    }
}