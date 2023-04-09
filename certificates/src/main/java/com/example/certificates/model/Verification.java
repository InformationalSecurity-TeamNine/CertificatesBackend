package com.example.certificates.model;



import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Verification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_code", length = 64)
    private String verificationCode;

    @Column(name = "expiration_date")
    private LocalDateTime expirationDate;


    public Verification(String verificationCode, LocalDateTime expirationDate) {
        this.verificationCode = verificationCode;
        this.expirationDate = expirationDate;
    }
}
