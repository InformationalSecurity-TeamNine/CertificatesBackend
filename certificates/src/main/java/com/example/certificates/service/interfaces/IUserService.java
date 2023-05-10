package com.example.certificates.service.interfaces;

import com.example.certificates.dto.PasswordResetDTO;
import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import com.example.certificates.enums.VerifyType;
import org.springframework.security.core.userdetails.UserDetails;


import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;

public interface IUserService {
    RegisteredUserDTO register(UserDTO registrationDTO) throws IOException, MessagingException;

    UserDetails findByUsername(String username);

    boolean getIsEmailConfirmed(String email);

    void verifyUser(String verificationCode);

    void sendPasswordResetCode(String email, VerifyType verifyType) throws MessagingException, UnsupportedEncodingException;

    void resetPassword(String email, PasswordResetDTO passwordResetDTO);
}
