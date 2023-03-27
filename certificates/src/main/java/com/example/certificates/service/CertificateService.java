package com.example.certificates.service;

import com.example.certificates.dto.CertificateDTO;

import com.example.certificates.dto.CertificateRequestDTO;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.Paginated;
import com.example.certificates.repository.CertificateRepository;
import com.example.certificates.security.JwtTokenUtil;
import com.example.certificates.security.UserRequestValidation;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
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
    public Paginated<CertificateDTO> getAll() {
        return null;
    }

    @Override
    public Paginated<CertificateDTO> getPastCertificates(Map<String, String> authHeader) {
        return null;
    }

    @Override
    public CertificateRequest createRequest(CertificateRequestDTO certificateRequest, Map<String, String> authHeader) {
        String role = this.userRequestValidation.getRoleFromToken(authHeader);

        if(role.equalsIgnoreCase("admin")){
            return null;
        }


        Long id = this.userRequestValidation.getIdFromToken(authHeader);
        return null;



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

}
