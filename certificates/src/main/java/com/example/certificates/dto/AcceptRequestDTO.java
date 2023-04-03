package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class AcceptRequestDTO {

    private LocalDateTime validFrom;
    private LocalDateTime validTo;
    private CertificateType type;
    private UserDTO issuer;
}
