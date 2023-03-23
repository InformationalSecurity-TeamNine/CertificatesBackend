package com.example.certificates.service;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
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
    public DeclineRequestDTO declineRequest(Long id) {
        return null;
    }

    @Override
    public AcceptRequestDTO acceptRequest(Long id) {
        return null;
    }
}
