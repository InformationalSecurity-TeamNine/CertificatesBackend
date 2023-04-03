package com.example.certificates.exceptions;

public class InvalidIssuerException extends RuntimeException {

    private String message;

    public InvalidIssuerException() {}

    public InvalidIssuerException(String msg)
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
