package com.example.certificates.service.interfaces;

import com.example.certificates.dto.*;


import com.example.certificates.model.CertificateRequest;

import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

public interface ICertificateService {

    List<CertificateUserDTO> getAll();

    List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader);

    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest, Map<String, String> authHeader);


    DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader);

    String acceptRequest(Long id, Map<String, String> authHeader);

    boolean isValid(Long id) throws CertificateEncodingException, NoSuchAlgorithmException, InvalidKeySpecException;

    CertificateWithdrawDTO withdraw(Long id, WithdrawReasonDTO withdrawReason, Map<String, String> headers);
}
