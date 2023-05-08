package com.example.certificates.dto;

import com.example.certificates.model.User;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WithdrawnCertificateDTO {

    private String serialNumber;
    private String reason;
    private LocalDateTime withdrawDate;
    private String userEmail;
    private boolean wasChild;
}
