package com.example.certificates.service;

import com.example.certificates.config.TwilioConfiguration;
import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import com.example.certificates.enums.UserRole;
import com.example.certificates.exceptions.CodeExpiredException;
import com.example.certificates.exceptions.InvalidRepeatPasswordException;
import com.example.certificates.exceptions.NonExistingVerificationCodeException;
import com.example.certificates.exceptions.UserAlreadyExistsException;
import com.example.certificates.model.User;
import com.example.certificates.model.Verification;
import com.twilio.rest.api.v2010.account.Message;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.Random;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    private final TwilioConfiguration twilioConfiguration;

    private final JavaMailSender mailSender;

    @Autowired
    public UserService(UserRepository userRepository, TwilioConfiguration twilioConfiguration, JavaMailSender mailSender){
        this.userRepository = userRepository;
        this.twilioConfiguration = twilioConfiguration;
        System.out.println(twilioConfiguration.getAccountSid());
        Twilio.init(twilioConfiguration.getAccountSid(), twilioConfiguration.getAuthToken());
        this.mailSender = mailSender;
    }

    @Override
    public RegisteredUserDTO register(UserDTO registrationDTO) throws UnsupportedEncodingException, MessagingException {

        checkValidUserInformation(registrationDTO);
        User user = getUserFromRegistrationDTO(registrationDTO);
        sendVerificationEmail(user);
        sendSms(user);
        return new RegisteredUserDTO(user);
    }

    private void sendSms(User user) {
        Message.creator(
                        new PhoneNumber(user.getTelephoneNumber()),
                        new PhoneNumber(twilioConfiguration.getPhoneNumber()),
                        "Your verification code is: " + user.getVerification().getVerificationCode())
                .create();
    }
    private void sendVerificationEmail(User user) throws MessagingException, UnsupportedEncodingException {
        String toAddress = user.getEmail();
        String fromAddress = "tim9certificates@gmail.com";
        String senderName = "Certificate app";
        String subject = "Verify the registration";
        String content = "Dear [[name]],<br>"
                + "Please click the link below to verify your registration:<br>"
                + "<h3><a href=\"[[URL]]\" target=\"_self\">VERIFY</a></h3>"
                + "Thank you,<br>"
                + "Certificate app.";

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message);

        helper.setFrom(fromAddress, senderName);
        helper.setTo(toAddress);
        helper.setSubject(subject);

        content = content.replace("[[name]]", user.getName());
        String verifyURL = "http://localhost:8082/api/user/activate/" + user.getVerification().getVerificationCode();

        content = content.replace("[[URL]]", verifyURL);

        helper.setText(content, true);

        mailSender.send(message);
    }

    private void checkValidUserInformation(UserDTO registrationDTO) {
        if(this.emailExists(registrationDTO.getEmail()))
        {
            throw new UserAlreadyExistsException("User with that email already exists!");
        }
        if(this.telephoneNumberExists(registrationDTO.getTelephoneNumber())){
            throw new UserAlreadyExistsException("User with that telephone number already exists!");
        }
        if(!registrationDTO.getPassword().equals(registrationDTO.getRepeatPassword())){
           throw new InvalidRepeatPasswordException("The passwords dont match!");
        }
    }

    @Override
    public UserDetails findByUsername(String username) {
        Optional<User> user = this.userRepository.findByEmail(username);
        if(user.isEmpty()) return null;
        return UserFactory.create(user.get());
    }

    @Override
    public boolean getIsEmailConfirmed(String email) {
        return this.userRepository.isEmailConfirmed(email).isPresent();
    }

    @Override
    public void verifyUser(String verificationCode) {
        User user = userRepository.findUserByVerification(verificationCode).orElse(null);

        if (user == null) {
            throw new NonExistingVerificationCodeException("That verification code does not exist!");
        } else if (user.getVerification().getExpirationDate().isBefore(LocalDateTime.now())) {
            throw new CodeExpiredException("Verification code expired. Register again!");
        } else {
            user.setEmailConfirmed(true);
            userRepository.save(user);
        }

    }

    private User getUserFromRegistrationDTO(UserDTO registrationDTO) {
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(UserRole.BASIC_USER);
        user.setTelephoneNumber(registrationDTO.getTelephoneNumber());
        user.setSurname(registrationDTO.getSurname());
        user.setName(registrationDTO.getName());
        user.setLastTimePasswordChanged(LocalDateTime.now());
        Random random = new Random();
        String code = String.format("%04d", random.nextInt(1000000));
        user.setVerification(new Verification(code, LocalDateTime.now().plusDays(3)));
        user.setEmailConfirmed(false);
        user = this.userRepository.save(user);
        return user;
    }

    private boolean emailExists(String email){
        Optional<User> user = this.userRepository.findByEmail(email);
        return user.isPresent();
    }
    private boolean telephoneNumberExists(String telephoneNumber){
        Optional<User> user = this.userRepository.findByTelephoneNumber(telephoneNumber);
        return user.isPresent();
    }
}
