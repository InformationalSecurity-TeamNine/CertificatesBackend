package com.example.certificates.service.interfaces;

import com.example.certificates.dto.CertificateRequestDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.Paginated;

public interface ICertificateService {

    Paginated<Certificate> getAll();
    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest);
}
