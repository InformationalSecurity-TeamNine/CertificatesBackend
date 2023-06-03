package com.example.certificates.model;

import lombok.*;

import javax.persistence.*;
import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;


@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@EqualsAndHashCode
@Entity
public class PastPasswords implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(cascade = {CascadeType.REFRESH}, fetch = FetchType.LAZY)
    private User user;

    private String password;
    private LocalDateTime timeChanged;

    public PastPasswords(User user, String password, LocalDateTime timeChanged){
        this.user = user;
        this.password = password;
        this.timeChanged = timeChanged;
    }

}
