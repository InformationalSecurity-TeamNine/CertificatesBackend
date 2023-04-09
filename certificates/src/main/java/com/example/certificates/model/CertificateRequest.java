package com.example.certificates.model;

import com.example.certificates.enums.CertificateType;
import com.example.certificates.enums.RequestStatus;
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
public class CertificateRequest implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    User issuer;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private Certificate parentCertificate;

    @Enumerated(EnumType.STRING)
    @Column
    private CertificateType certificateType;

    @Enumerated(EnumType.STRING)
    @Column
    private RequestStatus status;

    private LocalDateTime time;

    private String reason;

}
