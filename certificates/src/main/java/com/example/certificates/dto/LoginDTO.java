package com.example.certificates.dto;

import com.example.certificates.enums.VerifyType;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class LoginDTO {

    @Email(message = "{format}")
    @NotEmpty(message = "{required}")
    @Length(max=40, message = "{maxLength}")
    private String email;

    @NotEmpty(message = "{required}")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "{invalidPasswordFormat}")
    private String password;

    private VerifyType type;

}
