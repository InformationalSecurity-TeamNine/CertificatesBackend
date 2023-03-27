package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.Certificate;
import com.example.certificates.model.CertificateRequest;
import com.example.certificates.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.lang.Nullable;

import javax.persistence.*;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestDTO {

    private Long id;

    Date validTo;

    String issuerSN;

    String username;

    private CertificateType certificateType;

    public CertificateRequestDTO(CertificateRequest certificateRequest){
        this.id = certificateRequest.getId();
        this.username = certificateRequest.getIssuer().getEmail();
        this.issuerSN = certificateRequest.getParentCertificate().getId().toString();
        this.certificateType = certificateRequest.getCertificateType();

    }
}
