package com.example.certificates.model;

import com.example.certificates.enums.CertificateStatus;
import com.example.certificates.enums.CertificateType;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class Certificate implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private User user;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private Certificate issuingCertificate;

    private String serialNumber;

    private String signatureAlgorithm;

    private LocalDateTime validFrom;

    private LocalDateTime validTo;
    @Lob
    private String publicKey;

    @Enumerated(EnumType.STRING)
    @Column
    private CertificateStatus status;

    @Enumerated(EnumType.STRING)
    @Column
    private CertificateType type;


}

