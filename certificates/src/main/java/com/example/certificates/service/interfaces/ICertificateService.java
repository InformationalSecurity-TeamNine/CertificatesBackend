package com.example.certificates.service.interfaces;

import com.example.certificates.dto.CertificateDTO;


import com.example.certificates.dto.CertificateRequestDTO;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.Paginated;

public interface ICertificateService {

    Paginated<CertificateDTO> getAll();
    boolean isCertificateValid(Long id);

    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest);


    DeclineRequestDTO declineRequest(Long id, String declineReason);

    AcceptRequestDTO acceptRequest(Long id);

}
