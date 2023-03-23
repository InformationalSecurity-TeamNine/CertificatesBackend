package com.example.certificates.service.interfaces;

import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;

public interface IUserService {
    RegisteredUserDTO register(UserDTO registrationDTO);

    UserDetails findByUsername(String username);

    boolean getIsEmailConfirmed(String email);
}
