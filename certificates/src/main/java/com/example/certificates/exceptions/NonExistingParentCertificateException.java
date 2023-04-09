package com.example.certificates.exceptions;

public class NonExistingParentCertificateException extends RuntimeException{
    private String message;

    public NonExistingParentCertificateException() {}

    public NonExistingParentCertificateException(String msg)
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
