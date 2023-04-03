package com.example.certificates.model;

import lombok.*;
import org.bouncycastle.asn1.x500.X500Name;

import java.security.PublicKey;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@EqualsAndHashCode
@NoArgsConstructor
@AllArgsConstructor
public class SubjectData {

    private PublicKey publicKey;
    private X500Name x500name;
    private String serialNumber;
    private LocalDate startDate;
    private LocalDate endDate;

}
