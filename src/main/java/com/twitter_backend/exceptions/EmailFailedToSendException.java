package com.twitter_backend.exceptions;

public class EmailFailedToSendException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public EmailFailedToSendException() {
        super("The email failed to send");
    }

}
