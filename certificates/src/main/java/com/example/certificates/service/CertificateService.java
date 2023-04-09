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

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.math.BigInteger;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;
import java.time.LocalDateTime;
import java.util.*;

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
    public List<CertificateUserDTO> getAll() {
        List<CertificateDTO> certificates = this.certificateRepository.getAllCertificates();
        List<CertificateUserDTO> newCertificates = new ArrayList<>();
        for(CertificateDTO certificate: certificates){
            newCertificates.add(new CertificateUserDTO(certificate));
        }
        return newCertificates;
    }

    @Override
    public List<CertificateRequestResponse> getPastRequests(Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        if(role.equalsIgnoreCase("admin")){
            return this.certificateRequestRepository.getAllRequests();
        }
        Integer userId = this.userRequestValidation.getUserId(authHeader);
        return this.certificateRequestRepository.getRequestFromUser(userId.longValue());

    }

    @Override
    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest, Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        Integer userId = this.userRequestValidation.getUserId(authHeader);
        Certificate issuer = null;
        if (!certificateRequest.getIssuerSN().isEmpty()){
            issuer = this.certificateRepository.findByIssuerSN(certificateRequest.getIssuerSN());

        }
        if (issuer != null){
            issuer.setUser(this.certificateRepository.getUserByCertificateId(issuer.getId()));
            validateIssuerEndCertificate(certificateRequest, issuer);
            //validateCertificateEndDate(certificateRequest, issuer);
        }
        CertificateRequest request = new CertificateRequest();
        request.setIssuer(this.userRepository.findById(Long.valueOf(userId)).get());
        request.setStatus(RequestStatus.PENDING);
        request.setParentCertificate(issuer);
        request.setTime(certificateRequest.getTime());


        if(role.equalsIgnoreCase("admin")){

            if (issuer!=null){
                validateIssuer(certificateRequest);
            }
            request.setCertificateType(CertificateType.valueOf(certificateRequest.getType()));
            CertificateRequest newRequest = this.certificateRequestRepository.save(request);
            if(request.getParentCertificate() != null && userId.longValue() == request.getParentCertificate().getUser().getId()) {
                this.acceptRequest(newRequest.getId(), authHeader);
                newRequest.setStatus(RequestStatus.ACCEPTED);
            }
            else if(request.getParentCertificate() == null){
                this.acceptRequest(newRequest.getId(), authHeader);
                newRequest.setStatus(RequestStatus.ACCEPTED);
            }
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
    public boolean isValid(Long id) throws CertificateEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {

        Optional<Certificate> certificate = certificateRepository.findById(id);
        if(certificate.isEmpty())
            throw new NonExistingCertificateException("Certificate with the given ID does not exist.");

        if(isExpired(certificate.get())) return false;
        if(isWithdrawn(certificate.get())) return false;
        if(isStoredCertificateInvalid(id)) return false;

        return true;
    }

    private byte[] sign(byte[] data, PrivateKey privateKey) {
        try {
            // Kreiranje objekta koji nudi funkcionalnost digitalnog potpisivanja
            // Prilikom getInstance poziva prosledjujemo algoritam koji cemo koristiti
            // U ovom slucaju cemo generisati SHA-1 hes kod koji cemo potpisati upotrebom RSA asimetricne sifre
            Signature sig = Signature.getInstance("SHA1withRSA");

            // Navodimo kljuc kojim potpisujemo
            sig.initSign(privateKey);

            // Postavljamo podatke koje potpisujemo
            sig.update(data);

            // Vrsimo potpisivanje
            return sig.sign();
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return null;
    }

    private boolean verify(byte[] data, byte[] signature, PublicKey publicKey) {
        try {
            // Kreiranje objekta koji nudi funkcionalnost digitalnog potpisivanja
            // Prilikom getInstance poziva prosledjujemo algoritam koji cemo koristiti
            // U ovom slucaju cemo generisati SHA-1 hes kod koji cemo potpisati upotrebom RSA asimetricne sifre
            Signature sig = Signature.getInstance("SHA1withRSA");

            // Navodimo kljuc sa kojim proveravamo potpis
            sig.initVerify(publicKey);

            // Postavljamo podatke koje potpisujemo
            sig.update(data);

            // Vrsimo proveru digitalnog potpisa
            return sig.verify(signature);
        } catch (InvalidKeyException | NoSuchAlgorithmException | SignatureException e) {
            e.printStackTrace();
        }
        return false;
    }

    @SuppressWarnings("rawtypes")
    private java.security.cert.Certificate readFromBinEncFile(String sn) {
        try {
            String name = "certs/" + new BigInteger(sn.replace("-", ""), 16) + ".crt";

            FileInputStream fis = new FileInputStream(name);
            CertificateFactory cf = CertificateFactory.getInstance("X.509");
            Collection c = cf.generateCertificates(fis);
            Iterator i = c.iterator();
            while (i.hasNext()) {
                java.security.cert.Certificate cert = (java.security.cert.Certificate) i.next();
                return cert;
            }
        } catch (FileNotFoundException | CertificateException e) {
            return null;
        }
        return null;
    }
    private PublicKey convertStringToPublicKey(String key) throws NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] keyBytes = Base64.getDecoder().decode(key);
        X509EncodedKeySpec spec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance("RSA");
        PublicKey publicKey = keyFactory.generatePublic(spec);
        return publicKey;
    }

    private boolean isStoredCertificateInvalid(Long id) throws CertificateEncodingException, NoSuchAlgorithmException, InvalidKeySpecException {
        Optional<Certificate> certificate = this.certificateRepository.findById(id);
        if(certificate.isEmpty()){
            throw new NonExistingCertificateException("Certificate with that id does not exist");
        }
        String publicKey = certificate.get().getPublicKey();
        String sn = certificate.get().getSerialNumber();
        PrivateKey privateKey = this.certificateGeneratorService.getPrivateKey(sn);
        java.security.cert.Certificate certificate1 = readFromBinEncFile(sn);
        if (certificate1 == null){
            return true;
        }
        byte[] dataToSign = certificate1.getEncoded();
        byte[] signature = sign(dataToSign, privateKey);

        return !verify(dataToSign,signature,convertStringToPublicKey(publicKey));
    }

    private boolean isWithdrawn(Certificate certificate){
        return certificate.getStatus().toString().equals(CertificateStatus.NOT_VALID.toString());
    }

    private boolean isExpired(Certificate certificate){
        return  certificate.getValidTo().isBefore(LocalDateTime.now());
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

        if (request.get().getCertificateType()!=CertificateType.ROOT){
            if (this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId()) == null){
                throw new NonExistingParentCertificateException("Invalid parent Id");
            }

            if (userId.longValue() != this.certificateRequestRepository.getIssuerCertificateUserIdByRequestId(request.get().getId())) {
                throw new NonExistingRequestException("The request with the given id doesn't exist");
            }
        }

        if (request.get().getStatus()!=RequestStatus.PENDING){
            throw new RequestAlreadyProcessedException("The request has already been processed");
        }
        KeyPair keyPair = certificateGeneratorService.generateKeyPair();
        this.certificateGeneratorService.createCertificate(request.get(), keyPair);
        request.get().setStatus(RequestStatus.ACCEPTED);
        this.certificateRequestRepository.save(request.get());
        return "Request accepted";
    }





}
