package com.example.certificates.dto;

import com.example.certificates.enums.VerifyType;
import com.example.certificates.model.User;
import lombok.*;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Pattern;
import javax.validation.constraints.Size;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class UserDTO {

    @NotEmpty(message = "{required}")
    @Length(max=25, message = "{maxLength}")
    private String name;

    @NotEmpty(message = "{required}")
    @Length(max=25, message = "{maxLength}")
    private String surname;

    @NotEmpty(message = "{required}")
    @Length(max=20, message = "{maxLength}")
    private String telephoneNumber;

    @Email(message = "{format}")
    @NotEmpty(message = "{required}")
    @Length(max=40, message = "{maxLength}")
    private String email;


    /*
     *  ADD REGEX
     *
     * */
    @NotEmpty(message = "{required}")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$", message = "{invalidPasswordFormat}")
    private String password;

    @NotEmpty(message = "{required}")
    @Pattern(regexp = "^(?=.*?[A-Z])(?=.*?[a-z])(?=.*?[0-9])(?=.*?[#?!@$%^&*-]).{8,}$" , message = "{invalidPasswordFormat}")
    private String repeatPassword;

    private VerifyType verifyType;

    public UserDTO(User user){
        this.name = user.getName();
        this.surname = user.getSurname();
        this.telephoneNumber = user.getTelephoneNumber();
        this.email = user.getEmail();
        this.password = user.getPassword();
        this.repeatPassword = user.getPassword();
    }
}
