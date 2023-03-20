package com.example.certificates.exceptions;

public class InvalidRepeatPasswordException  extends RuntimeException{
    private String message;

    public InvalidRepeatPasswordException() {}

    public InvalidRepeatPasswordException(String msg)
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
