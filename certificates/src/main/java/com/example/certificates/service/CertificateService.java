package com.example.certificates.service;

import com.example.certificates.dto.CertificateDTO;

import com.example.certificates.dto.CertificateRequestDTO;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.Paginated;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class CertificateService implements ICertificateService {

    private CertificateRepository certificateRepository;

    @Autowired
    public CertificateService(CertificateRepository certificateRepository){
        this.certificateRepository = certificateRepository;
    }

    @Override
    public Paginated<CertificateDTO> getAll() {
        return null;
    }

    @Override

    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest) {
        return null;
        }

    public DeclineRequestDTO declineRequest(Long id, String declineReason) {
        DeclineRequestDTO declineRequestDTO =
                new DeclineRequestDTO(id,
                        declineReason);
        return declineRequestDTO;
    }

    @Override
    public AcceptRequestDTO acceptRequest(Long id) {
        AcceptRequestDTO acceptRequestDTO =
                new AcceptRequestDTO(LocalDateTime.now(),
                        LocalDateTime.now().plusMonths(3),
                        CertificateType.INTERMEDIATE, null);
        return acceptRequestDTO;
    }

    @Override
    public boolean isCertificateValid(Long id) {
        return false;
    }

}
