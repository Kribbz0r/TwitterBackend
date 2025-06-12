package com.twitter_backend.exceptions;

public class UserDoesntExistException extends RuntimeException {

    public UserDoesntExistException() {
        super("The username doesn't exist");
    }

}
