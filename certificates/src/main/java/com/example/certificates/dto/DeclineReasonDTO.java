package com.example.certificates.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeclineReasonDTO {

    @NotEmpty
    @Length(max=40, message = "{maxLength}")
    private String reason;
}
