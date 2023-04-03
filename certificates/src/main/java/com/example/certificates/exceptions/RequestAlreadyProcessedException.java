package com.example.certificates.exceptions;

public class RequestAlreadyProcessedException extends RuntimeException{

    private String message;

    public RequestAlreadyProcessedException() {}

    public RequestAlreadyProcessedException(String msg)
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

