package com.naveen.healthcare.claimservice.exception;

public class ClaimPolicyVerificationException
        extends RuntimeException {

    public ClaimPolicyVerificationException(
            String message
    ) {
        super(message);
    }
}
