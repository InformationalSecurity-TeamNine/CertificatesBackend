package com.example.certificates.exceptions;

public class NonExistingUserException extends RuntimeException{

    private String message;

    public NonExistingUserException() {}

    public NonExistingUserException(String msg)
    {
        super(msg);
        this.message = msg;
    }

    @Override
    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
