package com.example.certificates.service.interfaces;

import com.example.certificates.dto.RegistrationDTO;
import com.example.certificates.dto.UserDTO;

public interface IUserService {
    UserDTO register(RegistrationDTO registrationDTO);
}
