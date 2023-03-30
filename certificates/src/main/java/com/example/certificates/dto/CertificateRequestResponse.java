package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.enums.RequestStatus;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class CertificateRequestResponse {

    private Long id;

    private String issuerSN;

    private String username;

    private RequestStatus status;

    private CertificateType type;
}
