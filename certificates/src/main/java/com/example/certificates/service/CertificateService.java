package com.example.certificates.service;

import com.example.certificates.dto.CertificateDTO;

import com.example.certificates.dto.CertificateRequestDTO;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.enums.CertificateStatus;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.exceptions.EndIssuerException;
import com.example.certificates.exceptions.InvalidCertificateEndDateException;
import com.example.certificates.exceptions.InvalidIssuerException;
import com.example.certificates.exceptions.NonExistingCertificateException;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.Paginated;
import com.example.certificates.model.User;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.security.JwtTokenUtil;
import com.example.certificates.security.UserRequestValidation;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.FileInputStream;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class CertificateService implements ICertificateService {

    private final CertificateRepository certificateRepository;
    private final UserRequestValidation userRequestValidation;

    /*
        *
        * String role = this.userRequestValidation.getRoleFromToken(headers);
        if(role.equalsIgnoreCase("driver")){
            boolean areIdsEqual = this.userRequestValidation.areIdsEqual(headers, driverId);
            if(!areIdsEqual) return new ResponseEntity("Driver does not exist!", HttpStatus.NOT_FOUND);
        }
        *
        * */
    @Autowired
    public CertificateService(CertificateRepository certificateRepository, UserRequestValidation userRequestValidation){
        this.certificateRepository = certificateRepository;
        this.userRequestValidation = userRequestValidation;
    }

    @Override
    public List<CertificateDTO> getAll() {
        return this.certificateRepository.getAllCertificates();
    }

    @Override
    public List<CertificateDTO> getPastRequests(Map<String, String> authHeader) {
        return null;
    }

    @Override
    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest, Map<String, String> authHeader) {

        String role = this.userRequestValidation.getRoleFromToken(authHeader);
        Integer userId = this.userRequestValidation.getUserId(authHeader);

        Certificate issuer = this.certificateRepository.findByIssuerSN(certificateRequest.getIssuerSN());

        validateIssuerEndCertificate(certificateRequest, issuer);
        validateCertificateEndDate(certificateRequest, issuer);


        if(role.equalsIgnoreCase("admin")){

            CertificateRequest request = new CertificateRequest();

            /*
            * Svaki autentifikovani korisnik
            * ima mogućnost da napravi
            * zahteva za izdavanje sertifikata.
            * Korisnik može da zahteva
            * samo intermediate ili end
            * sertifikate, dok admin
            * može da zahteva i root sertifikat.
            *  Zahtev se prosleđuje onom korisniku
            *  koji je vlasnik sertifikata koji
            *  treba da potpiše sertifikat za
            * koji je podnet zahtev. Ako se
            * sertifikat izdaje na osnovu
            * sertifikata čiji je isti korisnik i
            * vlasnik, zahtev se automatski
            * odobrava. Ako se sertifikat izdaje
            *  na osnovu root sertifikata same
            *  aplikacije, admin mora da odobri
            * sertifikat. Ako admin zahteva da se
            *  napravi neki sertifikat na osnovu nekog
            * drugog sertifikata ili ako je to novi root
            * sertifikat, zahtev se automatski odobrava. (3 boda)

             *
            * */

            return null;
        }
        validateIssuer(certificateRequest);


        return null;



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
        DeclineRequestDTO declineRequestDTO =
                new DeclineRequestDTO(id,
                        declineReason);
        return declineRequestDTO;
    }

    @Override
    public AcceptRequestDTO acceptRequest(Long id, Map<String, String> authHeader) {
        AcceptRequestDTO acceptRequestDTO =
                new AcceptRequestDTO(LocalDateTime.now(),
                        LocalDateTime.now().plusMonths(3),
                        CertificateType.INTERMEDIATE, null);
        return acceptRequestDTO;
    }

    private boolean isCertificateValid(Long id) {
        return false;
    }

    private void validateCertificate(CertificateRequestDTO certificateRequest){

    }

}
