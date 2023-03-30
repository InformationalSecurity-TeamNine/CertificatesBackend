package com.example.certificates.service.interfaces;

import com.example.certificates.dto.*;


import com.example.certificates.model.CertificateRequest;

import java.util.List;
import java.util.Map;

public interface ICertificateService {

    List<CertificateDTO> getAll();

    List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader);

    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest, Map<String, String> authHeader);


    DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader);

    AcceptRequestDTO acceptRequest(Long id, Map<String, String> authHeader);

    boolean isValid(String serialNumber);

}
