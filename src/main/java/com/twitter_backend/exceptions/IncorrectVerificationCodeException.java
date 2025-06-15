package com.twitter_backend.exceptions;

public class IncorrectVerificationCodeException extends RuntimeException {
    public IncorrectVerificationCodeException() {
        super("The verificationcode sent didn't match the correct verificationcode");

    }

}
