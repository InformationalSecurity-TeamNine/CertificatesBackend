package com.example.certificates.dto;

import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class OauthUserDTO {
    @NotEmpty(message = "{required}")
    @Length(max=25, message = "{maxLength}")
    private String name;

    @NotEmpty(message = "{required}")
    @Length(max=25, message = "{maxLength}")
    private String surname;

    @Email(message = "{format}")
    @NotEmpty(message = "{required}")
    @Length(max=40, message = "{maxLength}")
    private String email;
}
