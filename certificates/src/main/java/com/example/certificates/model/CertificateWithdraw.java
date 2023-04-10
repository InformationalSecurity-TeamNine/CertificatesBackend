package com.example.certificates.model;

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
public class CertificateWithdraw implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    User user;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private Certificate certificate;

    private LocalDateTime withdrawnAt;
    private String reason;
    private boolean isChild;

    public CertificateWithdraw(User user, Certificate certificate, LocalDateTime now, String reason, boolean isChild) {
        this.user = user;
        this.certificate = certificate;
        this.withdrawnAt = now;
        this.reason = reason;
        this.isChild = isChild;
    }
}
