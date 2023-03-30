package com.example.certificates.service.interfaces;

import com.example.certificates.dto.CertificateDTO;


import com.example.certificates.dto.CertificateRequestDTO;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.model.CertificateRequest;

import java.util.List;
import java.util.Map;

public interface ICertificateService {

    List<CertificateDTO> getAll();

    List<CertificateDTO> getPastRequests(Map<String, String> authHeader);

    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest, Map<String, String> authHeader);


    DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader);

    AcceptRequestDTO acceptRequest(Long id, Map<String, String> authHeader);

    boolean isValid(String serialNumber);

}
