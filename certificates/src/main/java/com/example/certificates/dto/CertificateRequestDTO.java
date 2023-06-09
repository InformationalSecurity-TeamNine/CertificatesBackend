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
import java.time.LocalDateTime;
import java.util.Date;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestDTO {

    private Long id;

    private String issuerSN;

    private LocalDateTime time;


    private String type;



    public CertificateRequestDTO(CertificateRequest certificateRequest){
        this.id = certificateRequest.getId();
        if(certificateRequest.getParentCertificate() == null){
            this.issuerSN = "no issuer";
        }else{
            this.issuerSN = certificateRequest.getParentCertificate().getSerialNumber();
        }
        this.type = certificateRequest.getCertificateType().toString();
        this.time = certificateRequest.getTime();

    }
}
