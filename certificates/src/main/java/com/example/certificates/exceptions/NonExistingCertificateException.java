package com.example.certificates.exceptions;

public class NonExistingCertificateException extends RuntimeException {
    private String message;

    public NonExistingCertificateException() {}

    public NonExistingCertificateException(String msg)
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
