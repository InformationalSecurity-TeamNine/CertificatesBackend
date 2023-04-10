package com.example.certificates.exceptions;

public class NonExistingVerificationCodeException extends RuntimeException{
    private String message;

    public NonExistingVerificationCodeException() {}

    public NonExistingVerificationCodeException(String msg)
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
