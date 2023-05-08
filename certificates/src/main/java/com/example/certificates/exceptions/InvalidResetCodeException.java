package com.example.certificates.exceptions;

public class InvalidResetCodeException extends RuntimeException{

    private String message;

    public InvalidResetCodeException() {}

    public InvalidResetCodeException(String msg)
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
