package com.example.certificates.exceptions;

public class InvalidCertificateEndDateException extends RuntimeException {
    private String message;

    public InvalidCertificateEndDateException() {}

    public InvalidCertificateEndDateException(String msg)
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
