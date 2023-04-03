package com.example.certificates.exceptions;

public class EndIssuerException extends RuntimeException {
    private String message;

    public EndIssuerException() {}

    public EndIssuerException(String msg)
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
