package com.example.certificates.exceptions;

public class CodeExpiredException extends RuntimeException{

    private String message;

    public CodeExpiredException() {}

    public CodeExpiredException(String msg)
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
