package com.example.certificates.controller;
import com.example.certificates.dto.AcceptRequestDTO;
import com.example.certificates.dto.CertificateDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.Paginated;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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

        Paginated<CertificateDTO> allCertificates = this.certificateService.getAll();
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @PutMapping(value = "/accept-request/{id}")
    public ResponseEntity<AcceptRequestDTO> acceptRequest(@PathVariable Long id){
        return new ResponseEntity<>(null, HttpStatus.OK);
    }

    @PutMapping(value = "/decline-request/{id}")
    public ResponseEntity<DeclineRequestDTO> declineRequest(@PathVariable Long id){
        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
