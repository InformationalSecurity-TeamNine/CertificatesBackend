package com.example.certificates.controller;

import com.example.certificates.dto.*;
import com.example.certificates.model.CertificateRequest;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.dto.DeclineReasonDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.model.Paginated;
import com.example.certificates.security.UserRequestValidation;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;
import java.util.Map;

@CrossOrigin
@RestController
@RequestMapping("api/certificate")
public class CertificateController {

    private final ICertificateService certificateService;

    @Autowired
    public CertificateController(ICertificateService certificateService){
        this.certificateService = certificateService;
    }
    @GetMapping
    public ResponseEntity<List<CertificateDTO>> getCertificates(){

        List<CertificateDTO> allCertificates = this.certificateService.getAll();
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @GetMapping(value = "/past-requests/")
    public ResponseEntity<List<CertificateDTO>> getPastCertificates(
            @RequestHeader Map<String, String> headers){

        List<CertificateDTO> allCertificates = this.certificateService.getPastRequests(headers);
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @GetMapping(value = "/valid/{serial-number}")
    public ResponseEntity<Boolean> getCertificateValidation(
            @PathVariable("serial-number")String serialNumber){

        boolean isValid = this.certificateService.isValid(serialNumber);
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }

    @PutMapping(value = "/accept-request/{id}")
    public ResponseEntity<AcceptRequestDTO> acceptRequest(@PathVariable Long id,
                                                          @RequestHeader Map<String, String> headers){

        AcceptRequestDTO acceptRequest = this.certificateService.acceptRequest(id, headers);

        return new ResponseEntity<>(acceptRequest, HttpStatus.OK);
    }

    @PutMapping(value = "/decline-request/{id}")
    public ResponseEntity<DeclineRequestDTO> declineRequest(
            @PathVariable Long id,
            @Valid @RequestBody DeclineReasonDTO declineReason,
            @RequestHeader Map<String, String> headers){
        DeclineRequestDTO declineRequest = this.certificateService.declineRequest(id, declineReason.getReason(), headers);

        return new ResponseEntity<>(declineRequest, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateRequestDTO> create(
            @Valid @RequestBody CertificateRequestDTO certificateRequest,
            @RequestHeader Map<String, String> headers) {

        CertificateRequest newRequest = this.certificateService.createRequest(certificateRequest, headers);
        return new ResponseEntity<>(new CertificateRequestDTO(newRequest), HttpStatus.OK);
    }
}
