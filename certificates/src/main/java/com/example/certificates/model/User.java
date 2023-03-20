package com.example.certificates.model;

import com.example.certificates.enums.UserRole;
import lombok.*;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@ToString
@Table(name = "Users")
public class User implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String email;

    private String password;

    private String name;

    private String surname;

    @Column
    private String telephoneNumber;

    @Enumerated(EnumType.STRING)
    @Column
    private UserRole role;

    private boolean isEmailConfirmed;

}
