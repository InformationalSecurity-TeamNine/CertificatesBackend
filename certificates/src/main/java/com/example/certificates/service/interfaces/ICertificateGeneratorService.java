package com.example.certificates.service.interfaces;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;

import java.security.KeyPair;
import java.time.LocalDateTime;

public interface ICertificateGeneratorService {
    public KeyPair generateKeyPair();
    public LocalDateTime getExpirationDate(Certificate parentCertificate, CertificateType type);

    public Certificate createCertificate(CertificateRequest certificateRequest, KeyPair keyPair);
}
