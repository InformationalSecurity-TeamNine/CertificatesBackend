package com.example.certificates.exceptions;

public class InvalidFileException extends RuntimeException{
    private String message;

    public InvalidFileException() {}

    public InvalidFileException(String msg)
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
