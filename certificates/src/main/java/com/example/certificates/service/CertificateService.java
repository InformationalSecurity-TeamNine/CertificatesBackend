package com.example.certificates.service;

import com.example.certificates.dto.*;

import com.example.certificates.enums.CertificateStatus;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.enums.RequestStatus;
import com.example.certificates.exceptions.*;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.repository.CertificateRequestRepository;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.security.UserRequestValidation;
import com.example.certificates.service.interfaces.ICertificateGeneratorService;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Service
public class CertificateService implements ICertificateService {
    private final CertificateRepository certificateRepository;
    private final CertificateRequestRepository certificateRequestRepository;
    private final UserRepository userRepository;
    private final UserRequestValidation userRequestValidation;
    private final ICertificateGeneratorService certificateGeneratorService;


    @Autowired
    public CertificateService(CertificateRepository certificateRepository, CertificateRequestRepository certificateRequestRepository, UserRepository userRepository, UserRequestValidation userRequestValidation, ICertificateGeneratorService certificateGeneratorService){
        this.certificateRepository = certificateRepository;
        this.certificateRequestRepository = certificateRequestRepository;
        this.userRepository = userRepository;
        this.userRequestValidation = userRequestValidation;
        this.certificateGeneratorService = certificateGeneratorService;
    }

    @Override
    public List<CertificateDTO> getAll() {
        return this.certificateRepository.getAllCertificates();
    }

    @Override
    public List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        if(role.equalsIgnoreCase("admin")){
            return this.certificateRequestRepository.getAllRequests();
        }
        Integer userId = this.userRequestValidation.getUserId(authHeader);
        return this.certificateRequestRepository.getRequestFromUser(userId);

    }

    @Override
    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest, Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        Integer userId = this.userRequestValidation.getUserId(authHeader);

        Certificate issuer = this.certificateRepository.findByIssuerSN(certificateRequest.getIssuerSN());
        issuer.setUser(this.certificateRepository.getUserByCertificateId(issuer.getId()));

        validateIssuerEndCertificate(certificateRequest, issuer);
        validateCertificateEndDate(certificateRequest, issuer);

        CertificateRequest request = new CertificateRequest();
        request.setIssuer(this.userRepository.findById(Long.valueOf(userId)).get());
        request.setStatus(RequestStatus.PENDING);
        request.setParentCertificate(issuer);


        if(role.equalsIgnoreCase("admin")){

            validateIssuer(certificateRequest);
            request.setCertificateType(CertificateType.valueOf(certificateRequest.getType()));
            CertificateRequest newRequest = this.certificateRequestRepository.save(request);
            this.acceptRequest(newRequest.getId(), authHeader);
            newRequest.setStatus(RequestStatus.ACCEPTED);
            return newRequest;
        }
        if(certificateRequest.getType().toString().equalsIgnoreCase(CertificateType.ROOT.toString())){
            throw new InvalidCertificateTypeException("Cannot create root certificate as a default user");
        }
        validateIssuer(certificateRequest);
        request.setCertificateType(CertificateType.valueOf(certificateRequest.getType()));
        CertificateRequest newRequest = this.certificateRequestRepository.save(request);
        if(issuer.getUser().getId() == userId.longValue()){
            this.acceptRequest(newRequest.getId(), authHeader);
        }
        return newRequest;
    }
    @Override
    public boolean isValid(String serialNumber){
        Certificate certificate = certificateRepository.findByIssuerSN(serialNumber);
        if(certificate == null)
            throw new NonExistingCertificateException("Certificate with the given serial number does not exist.");

        if(isExpired(certificate)) return false;
        if(isWithdrawn(certificate)) return false;

        return true;
    }

    private boolean isWithdrawn(Certificate certificate){
        return certificate.getStatus().toString().equals(CertificateStatus.NOT_VALID.toString());
    }

    private boolean isExpired(Certificate certificate){
        return certificate.getValidTo().isBefore(LocalDateTime.now());
    }

    private void validateCertificateEndDate(CertificateRequestDTO certificateRequest, Certificate issuer) {
        if(certificateRequest.getValidTo().isAfter(issuer.getValidTo())){
            throw new InvalidCertificateEndDateException("Certificate end date must be before issuer end date.");

        }
    }

    private void validateIssuerEndCertificate(CertificateRequestDTO certificateRequest, Certificate issuer) {
        if(certificateRequest.getIssuerSN().length() > 0) {
            if(issuer.getType().toString().equals(CertificateType.END.toString()))
                throw new EndIssuerException("Type of issuer certificate cannot be end.");
        }
    }

    private void validateIssuer(CertificateRequestDTO certificateRequest) {
        if(certificateRequest.getIssuerSN().isEmpty()){
            throw new InvalidIssuerException("Issuer cannot be null for intermediate or end certificates.");
        }
    }

    public DeclineRequestDTO declineRequest(Long id, String declineReason, Map<String, String> authHeader) {

        Integer userId = this.userRequestValidation.getUserId(authHeader);

        Optional<CertificateRequest> request = this.certificateRequestRepository.findById(id);
        if(request.isEmpty()) throw new NonExistingRequestException("The request with the given id doesn't exist");


        if(userId.longValue() != this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId())){
            throw new NonExistingRequestException("The request with the given id doesn't exist");
        }

        if (request.get().getStatus()!=RequestStatus.PENDING){
            throw new RequestAlreadyProcessedException("The request has already been processed");
        }

        request.get().setStatus(RequestStatus.DECLINED);
        request.get().setReason(declineReason);
        DeclineRequestDTO declineRequestDTO = new DeclineRequestDTO(request.get().getId(), declineReason);
        this.certificateRequestRepository.save(request.get());


        return declineRequestDTO;
    }

    @Override
    public String acceptRequest(Long id, Map<String, String> authHeader) {

        Integer userId = this.userRequestValidation.getUserId(authHeader);
        Optional<CertificateRequest> request = this.certificateRequestRepository.findById(id);
        if(request.isEmpty()) throw new NonExistingRequestException("The request with the given id doesn't exist");
        if(userId.longValue() != this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId())){
            throw new NonExistingRequestException("The request with the given id doesn't exist");
        }
        if (request.get().getStatus()!=RequestStatus.PENDING){
            throw new RequestAlreadyProcessedException("The request has already been processed");
        }
        request.get().setStatus(RequestStatus.ACCEPTED);
        KeyPair keyPair = certificateGeneratorService.generateKeyPair();
        Certificate certificate = this.certificateGeneratorService.createCertificate(request.get(), keyPair);
        this.certificateRequestRepository.save(request.get());
        return "Request accepted";
    }





}
