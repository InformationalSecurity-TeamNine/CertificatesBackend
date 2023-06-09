package com.example.certificates.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class CertificateWithdrawDTO {

    private Long certificateId;
    private String reason;

}
