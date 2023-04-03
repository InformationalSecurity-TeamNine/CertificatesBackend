package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.Lob;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateUserDTO {
    private Long id;

    private LocalDateTime issuedAt;

    private UserForCertificateDTO user;

    private CertificateType type;
    @Lob
    private String serialNumber;

    private LocalDateTime validTo;

    public CertificateUserDTO(CertificateDTO certificate){
        this.id = certificate.getId();
        this.issuedAt = certificate.getIssuedAt();
        this.user = new UserForCertificateDTO(certificate.getUser().getName(), certificate.getUser().getSurname(), certificate.getUser().getEmail());
        this.type = certificate.getType();
        this.serialNumber = certificate.getSerialNumber();
        this.validTo = certificate.getValidTo();
    }

}
