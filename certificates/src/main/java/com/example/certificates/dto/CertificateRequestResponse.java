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

}
