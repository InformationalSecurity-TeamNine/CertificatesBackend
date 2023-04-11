package com.example.certificates.model;

import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
public class ResetCode {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column
    String code;

    @Column
    LocalDateTime expirationDate;

    public ResetCode(String resetCode, LocalDateTime expirationDate) {
        this.code = resetCode;
        this.expirationDate = expirationDate;
    }
}
