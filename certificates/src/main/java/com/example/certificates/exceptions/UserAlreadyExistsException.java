package com.example.certificates.exceptions;

public class UserAlreadyExistsException extends RuntimeException{

    private String message;

    public UserAlreadyExistsException() {}

    public UserAlreadyExistsException(String msg)
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
