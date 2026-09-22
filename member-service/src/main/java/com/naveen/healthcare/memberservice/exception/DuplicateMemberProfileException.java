package com.naveen.healthcare.memberservice.exception;

public class DuplicateMemberProfileException
        extends RuntimeException {

    public DuplicateMemberProfileException(
            String message
    ) {
        super(message);
    }
}