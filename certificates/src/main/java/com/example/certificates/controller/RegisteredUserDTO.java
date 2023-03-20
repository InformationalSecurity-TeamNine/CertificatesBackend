package com.example.certificates.controller;
import com.example.certificates.model.User;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
public class RegisteredUserDTO {

    private Long id;
    private String name;

    private String surname;

    private String telephoneNumber;

    private String email;

    public RegisteredUserDTO(User user){
        this.id = user.getId();
        this.name = user.getName();
        this.surname = user.getSurname();
        this.telephoneNumber = user.getTelephoneNumber();
        this.email = user.getEmail();
    }
}
