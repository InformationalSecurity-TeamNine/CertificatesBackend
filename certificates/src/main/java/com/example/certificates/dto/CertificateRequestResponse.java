package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.enums.RequestStatus;
import lombok.*;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestResponse {

    private Long id;

    private String username;

    private RequestStatus status;

    private CertificateType type;

    private String declineReason;
    private String issuerUsername;

    public CertificateRequestResponse(Long id, String username, RequestStatus status, CertificateType type, String declineReason){
        this.id = id;
        this.username = username;
        this.status = status;
        this.declineReason = declineReason;
        this.type = type;
        this.issuerUsername = "/";
    }

}
