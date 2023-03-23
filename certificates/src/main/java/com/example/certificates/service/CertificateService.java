package com.example.certificates.service;

import com.example.certificates.model.Certificate;
import com.example.certificates.model.Paginated;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.stereotype.Service;

@Service
public class CertificateService implements ICertificateService {
    
    @Override
    public Paginated<Certificate> getAll() {
        return null;
    }

    @Override
    public boolean isCertificateValid(Long id) {
        return false;
    }

}
