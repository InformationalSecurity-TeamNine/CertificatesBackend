package com.example.certificates.controller;

import com.example.certificates.dto.*;
import com.example.certificates.model.CertificateRequest;

import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.dto.DeclineReasonDTO;
import com.example.certificates.dto.DeclineRequestDTO;

import com.example.certificates.model.Paginated;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

@CrossOrigin
@RestController
@RequestMapping("api/certificate")
public class CertificateController {

    private ICertificateService certificateService;

    @Autowired
    public CertificateController(ICertificateService certificateService){
        this.certificateService = certificateService;
    }
    @GetMapping
    public ResponseEntity<Paginated<CertificateDTO>> getCertificates(){

        return new ResponseEntity<>(
                new Paginated<>(0, new HashSet<>()),
                                HttpStatus.OK);
    }

    @PutMapping(value = "/accept-request/{id}")
    public ResponseEntity<AcceptRequestDTO> acceptRequest(@PathVariable Long id){
        AcceptRequestDTO acceptRequest = this.certificateService.acceptRequest(id);

        return new ResponseEntity<>(acceptRequest, HttpStatus.OK);
    }

    @PutMapping(value = "/decline-request/{id}")
    public ResponseEntity<DeclineRequestDTO> declineRequest(
            @PathVariable Long id,
            @RequestBody DeclineReasonDTO declineReason){
        DeclineRequestDTO declineRequest = this.certificateService.declineRequest(id, declineReason);

        return new ResponseEntity<>(declineRequest, HttpStatus.OK);
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<CertificateRequestDTO> create(@Valid @RequestBody CertificateRequestDTO certificateRequest) {
        CertificateRequest newRequest = this.certificateService.createRequest(certificateRequest);
        return new ResponseEntity<>(new CertificateRequestDTO(newRequest), HttpStatus.OK);
    }
}
