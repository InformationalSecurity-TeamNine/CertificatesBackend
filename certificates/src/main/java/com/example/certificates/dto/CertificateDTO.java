package com.example.certificates.dto;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.model.User;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CertificateDTO {
    private Long id;

    private LocalDateTime issuedAt;

    private User user;

    private CertificateType type;

    private String serialNumber;


}
