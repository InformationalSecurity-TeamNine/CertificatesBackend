package com.example.certificates.controller;

import com.example.certificates.dto.*;
import com.example.certificates.enums.CertificateType;
import com.example.certificates.exceptions.NonExistingCertificateException;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;

import com.example.certificates.dto.DeclineReasonDTO;
import com.example.certificates.dto.DeclineRequestDTO;
import com.example.certificates.service.interfaces.ICertificateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.InputStreamResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.transaction.Transactional;
import javax.validation.Valid;
import java.io.*;
import java.security.*;
import java.security.cert.CertificateEncodingException;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.spec.InvalidKeySpecException;
import java.util.*;

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
    public ResponseEntity<List<CertificateUserDTO>> getCertificates(){

        List<CertificateUserDTO> allCertificates = this.certificateService.getAll();
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @GetMapping(value = "/past-requests/")
    public ResponseEntity<List<CertificateRequestResponse>> getPastCertificates(
            @RequestHeader Map<String, String> headers){

        List<CertificateRequestResponse> allCertificates = this.certificateService.getPastRequests(headers);
        return new ResponseEntity<>(allCertificates, HttpStatus.OK);
    }

    @GetMapping(value = "/valid/{id}")
    public ResponseEntity<Boolean> getCertificateValidation(
            @PathVariable("id")Long id){

        boolean isValid = this.certificateService.isValid(id);
        return new ResponseEntity<>(isValid, HttpStatus.OK);
    }

    @GetMapping(value = "/withdrawn/")
    public ResponseEntity<List<WithdrawnCertificateDTO>> getWithdrawnCertificates(
            ){
;
       List<WithdrawnCertificateDTO> certificates = this.certificateService.getWithdrawnCertificates();

        return new ResponseEntity<>(certificates, HttpStatus.OK);
    }

    @PutMapping(value = "/accept-request/{id}")
    public ResponseEntity<String> acceptRequest(@PathVariable Long id,
                                                          @RequestHeader Map<String, String> headers){

        String acceptRequest = this.certificateService.acceptRequest(id, headers);

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
        if (!Objects.equals(certificateRequest.getType(), CertificateType.END.toString())
                && !Objects.equals(certificateRequest.getType(), CertificateType.INTERMEDIATE.toString())
                && !Objects.equals(certificateRequest.getType(), CertificateType.ROOT.toString())){
            return new ResponseEntity("Bad request body", HttpStatus.BAD_REQUEST);
        }
        if (Objects.equals(certificateRequest.getType(), CertificateType.END.toString()) || Objects.equals(certificateRequest.getType(), CertificateType.INTERMEDIATE.toString())){
            if(certificateRequest.getIssuerSN().isEmpty()){
                return new ResponseEntity("Bad request body", HttpStatus.BAD_REQUEST);
            }
        }
        CertificateRequest newRequest = this.certificateService.createRequest(certificateRequest, headers);
        return new ResponseEntity<>(new CertificateRequestDTO(newRequest), HttpStatus.OK);
    }


    @PutMapping("/withdraw/{id}")
    public ResponseEntity<CertificateWithdrawDTO> withdrawCertificate(
            @PathVariable Long id,
            @Valid @RequestBody WithdrawReasonDTO withdrawReason,
            @RequestHeader Map<String, String> headers
    ){

        CertificateWithdrawDTO withdraw = this.certificateService.withdraw(id, withdrawReason, headers);

        return new ResponseEntity<>(withdraw, HttpStatus.OK);

    }


    @GetMapping("download/{id}")
    public ResponseEntity<InputStreamResource> downloadCertificate(@PathVariable Long id){

        String fileName = this.certificateService.findCertificateFileName(id);


        File file = new File(fileName);
        InputStream inputStream = null;

        try {
            inputStream = new FileInputStream(file);
        } catch (FileNotFoundException e) {
            return null;
        }

        HttpHeaders headers = new HttpHeaders();
        headers.add("Content-Disposition", "attachment; filename=" + file.getName());

        return ResponseEntity.ok()
                .headers(headers)
                .contentLength(file.length())
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .body(new InputStreamResource(inputStream));
    }

    @Transactional
    @PostMapping("validate-upload")
    public ResponseEntity<Boolean> validateUploadedCertificate(@RequestParam MultipartFile file){



        X509Certificate certX509 = this.certificateService.getX509CertificateFromFile(file);

        Certificate cert = this.certificateService.getCertificateFromX509Certificate(certX509);

        boolean isValid = this.certificateService.isValid(cert.getId());
        if(!isValid) return new ResponseEntity<>(false, HttpStatus.OK);

        boolean isValidUploaded = this.certificateService.isUploadedInvalid(certX509, cert);
        if(!isValidUploaded) return new ResponseEntity<>(false, HttpStatus.OK);


        System.out.println("Da li je validan na serveru:" + isValid);
        System.out.println("Da li je uploaded validan:" + isValidUploaded);
        return new ResponseEntity<>(true, HttpStatus.OK);
    }


}
