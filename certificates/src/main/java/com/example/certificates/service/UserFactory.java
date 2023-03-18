package com.example.certificates.service;

import com.example.certificates.model.User;
import com.example.certificates.security.SecurityUser;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;

import java.util.Collection;

public class UserFactory {

    public static SecurityUser create(User user) {

        Collection<? extends GrantedAuthority> authorities;
        try {
            authorities = AuthorityUtils.commaSeparatedStringToAuthorityList(user.getRole().toString());
        } catch (Exception e) {
            authorities = null;
        }

        return new SecurityUser(
                user.getId(),
                user.getEmail(),
                user.getPassword(),
                authorities
        );
    }
}
