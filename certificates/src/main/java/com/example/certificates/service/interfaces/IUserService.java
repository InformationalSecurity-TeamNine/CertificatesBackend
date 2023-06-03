package com.example.certificates.service.interfaces;

import com.example.certificates.dto.*;
import com.example.certificates.enums.VerifyType;
import com.example.certificates.model.User;
import org.springframework.security.core.userdetails.UserDetails;


import javax.mail.MessagingException;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

public interface IUserService {
    RegisteredUserDTO register(UserDTO registrationDTO) throws IOException, MessagingException;

    UserDetails findByUsername(String username);

    boolean getIsEmailConfirmed(String email);

    void verifyUser(String verificationCode);

    void sendPasswordResetCode(String email, VerifyType verifyType) throws MessagingException, IOException;

    void resetPassword(String email, PasswordResetDTO passwordResetDTO);
    void sendLoginVerification(String email, VerifyType verifyType) throws MessagingException, IOException;

    void loginVerify(String email, LoginVerifyCodeDTO code);

    boolean verifyRecaptcha(String recaptcha);

    boolean isPasswordDurationValid(String email);

    Boolean oauthDoesMailExists(OauthUserDTO userDTO);

    RegisteredUserDTO regsterOauth(OauthUserDTO userDTO);
}
