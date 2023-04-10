package com.example.certificates.dto;


import lombok.*;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class WithdrawReasonDTO {

    @NotEmpty
    private String reason;
}
