package com.example.resto_backend.exception;

public class InvalidUsernameOrPasswordException extends RuntimeException {

    public InvalidUsernameOrPasswordException() {
        super("Invalid username or password");
    }

    public InvalidUsernameOrPasswordException(String message) {
        super(message);
    }
}
