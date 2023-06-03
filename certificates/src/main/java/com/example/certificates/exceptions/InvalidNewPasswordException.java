package com.example.certificates.exceptions;

public class InvalidNewPasswordException extends RuntimeException{
    private String message;

    public InvalidNewPasswordException() {}

    public InvalidNewPasswordException(String msg)
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
