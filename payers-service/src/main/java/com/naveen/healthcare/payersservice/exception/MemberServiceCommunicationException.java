package com.naveen.healthcare.payersservice.exception;

public class MemberServiceCommunicationException
        extends RuntimeException {

    public MemberServiceCommunicationException(
            String message
    ) {
        super(message);
    }

    public MemberServiceCommunicationException(
            String message,
            Throwable cause
    ) {
        super(message, cause);
    }
}
