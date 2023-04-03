package com.example.certificates.exceptions;

public class InvalidCertificateTypeException extends RuntimeException {

    private String message;

    public InvalidCertificateTypeException() {}

    public InvalidCertificateTypeException(String msg)
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
