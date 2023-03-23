package com.example.certificates.service.interfaces;

import com.example.certificates.model.Certificate;
import com.example.certificates.model.Paginated;

public interface ICertificateService {

    Paginated<Certificate> getAll();

    boolean isCertificateValid(Long id);
}
