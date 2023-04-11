package com.example.certificates.service.interfaces;

import com.example.certificates.dto.PasswordResetDTO;
import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import org.springframework.security.core.userdetails.UserDetails;


import javax.mail.MessagingException;
import java.io.UnsupportedEncodingException;

public interface IUserService {
    RegisteredUserDTO register(UserDTO registrationDTO) throws UnsupportedEncodingException, MessagingException;

    UserDetails findByUsername(String username);

    boolean getIsEmailConfirmed(String email);

    void verifyUser(String verificationCode);

    void sendPasswordResetCode(String email) throws MessagingException, UnsupportedEncodingException;

    void resetPassword(String email, PasswordResetDTO passwordResetDTO);
}
