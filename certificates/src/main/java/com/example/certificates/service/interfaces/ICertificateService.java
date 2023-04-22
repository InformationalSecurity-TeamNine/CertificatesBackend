package com.example.certificates.service.interfaces;

import com.example.certificates.dto.*;


import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.security.NoSuchAlgorithmException;
import java.security.cert.CertificateEncodingException;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.List;
import java.util.Map;

public interface ICertificateService {

    List<CertificateUserDTO> getAll();

    List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader);

    CertificateRequest createRequest(CertificateRequestDTO CertificateRequest, Map<String, String> authHeader);


    DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader);

    String acceptRequest(Long id, Map<String, String> authHeader);

    boolean isValid(Long id);

    String findCertificateFileName(Long id);

    X509Certificate getX509CertificateFromFile(MultipartFile file);

    CertificateWithdrawDTO withdraw(Long id, WithdrawReasonDTO withdrawReason, Map<String, String> headers);

    List<WithdrawnCertificateDTO> getWithdrawnCertificates();

    Certificate getCertificateFromX509Certificate(X509Certificate certX509);

    boolean isUploadedInvalid(X509Certificate certX509, Certificate cert);
}
