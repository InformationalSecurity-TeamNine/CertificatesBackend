package com.example.certificates.exceptions;

public class CertificateWithdrawnException extends RuntimeException {
    private String message;

    public CertificateWithdrawnException() {}

    public CertificateWithdrawnException(String msg)
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
