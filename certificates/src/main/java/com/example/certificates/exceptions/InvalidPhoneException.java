package com.example.certificates.exceptions;

public class InvalidPhoneException extends RuntimeException{

    private String message;

    public InvalidPhoneException() {}

    public InvalidPhoneException(String msg)
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
