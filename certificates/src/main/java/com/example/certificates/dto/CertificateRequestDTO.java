package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestDTO {

    private Long id;

    User issuer;

    private Certificate parentCertificate;

    private CertificateType certificateType;

    public CertificateRequestDTO(CertificateRequest certificateRequest){
        this.id = certificateRequest.getId();
        this.issuer = certificateRequest.getIssuer();
        this.parentCertificate = certificateRequest.getParentCertificate();
        this.certificateType = certificateRequest.getCertificateType();

    }
}
