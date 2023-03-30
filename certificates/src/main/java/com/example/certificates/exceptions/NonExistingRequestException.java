package com.example.certificates.exceptions;

public class NonExistingRequestException extends RuntimeException {
    private String message;

    public NonExistingRequestException() {}

    public NonExistingRequestException(String msg)
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
