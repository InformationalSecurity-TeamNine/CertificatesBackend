package com.example.certificates.service;

import com.example.certificates.config.TwilioConfiguration;
import com.example.certificates.controller.UserController;
import com.example.certificates.dto.*;
import com.example.certificates.enums.UserRole;
import com.example.certificates.enums.VerifyType;
import com.example.certificates.exceptions.*;
import com.example.certificates.model.*;
import com.example.certificates.repository.PastPasswordRepository;
import com.example.certificates.security.SecurityUser;
import com.example.certificates.security.UserRequestValidation;
import com.sendgrid.helpers.mail.Mail;
import com.sendgrid.helpers.mail.objects.Content;
import com.sendgrid.helpers.mail.objects.Email;
import com.twilio.rest.api.v2010.account.Message;
import io.github.cdimascio.dotenv.Dotenv;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpMethod;
import org.springframework.mail.javamail.JavaMailSender;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.twilio.Twilio;
import com.twilio.type.PhoneNumber;

import javax.mail.MessagingException;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Random;
import com.sendgrid.*;
import org.springframework.web.client.RestTemplate;

@Service
public class UserService implements IUserService {

    private final UserRepository userRepository;

    private final TwilioConfiguration twilioConfiguration;

    private final JavaMailSender mailSender;
    private final RestTemplate restTempate;
    private final UserRequestValidation userRequestValidation;
    private final PastPasswordRepository pastPasswordRepository;
    private final PasswordEncoder passwordEncoder;
    private static final Logger logger = LoggerFactory.getLogger(UserService.class);


    @Autowired
    public UserService(UserRepository userRepository, TwilioConfiguration twilioConfiguration, JavaMailSender mailSender, RestTemplate restTempate, UserRequestValidation userRequestValidation, PastPasswordRepository pastPasswordRepository, PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.twilioConfiguration = twilioConfiguration;
        this.restTempate = restTempate;
        this.userRequestValidation = userRequestValidation;
        this.pastPasswordRepository = pastPasswordRepository;
        this.passwordEncoder = passwordEncoder;
        Dotenv dotenv = Dotenv.load();
        Twilio.init(dotenv.get("TWILIO_ACCOUNT_SID"), dotenv.get("TWILIO_AUTH_TOKEN"));
        this.mailSender = mailSender;
    }

    @Override
    public RegisteredUserDTO register(UserDTO registrationDTO) throws IOException, MessagingException {

        checkValidUserInformation(registrationDTO);
        User user = getUserFromRegistrationDTO(registrationDTO);

        return new RegisteredUserDTO(user);
    }

    private void sendSmsVerification(User user) {
        try {
            Message.creator(
                            new PhoneNumber(user.getTelephoneNumber()),
                            new PhoneNumber(twilioConfiguration.getPhoneNumber()),
                            "Your verification code is: " + user.getVerification().getVerificationCode())
                    .create();
        }
        catch (com.twilio.exception.ApiException ex)
        {
            throw new InvalidPhoneException("Can't send message if phone is not verified and valid!");
        }
    }
    private void sendVerificationEmail(User user) throws MessagingException, IOException {
        Email from = new Email("tim9certificates@gmail.com");
        String subject = "Verify the registration";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/plain", "Dear " + user.getName() + ","
                + "Please click the link below to verify your registration: \n"
                + "http://localhost:8082/api/user/activate/" + user.getVerification().getVerificationCode() + "\n"
                + "Thank you,\n"
                + "Certificate app.");
        Mail mail = new Mail(from, subject, to, content);
        Dotenv dotenv = Dotenv.load();
        SendGrid sg = new SendGrid(dotenv.get("SENDGRID_API_KEY"));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }

    private void checkValidUserInformation(UserDTO registrationDTO) {
        if(this.emailExists(registrationDTO.getEmail()))
        {
            logger.error("Neregistrovan korisnik je pokusao da se registruje sa vec postojecim mejlom.");

            throw new UserAlreadyExistsException("User with that email already exists!");
        }
        if(this.telephoneNumberExists(registrationDTO.getTelephoneNumber())){
            logger.error("Neregistrovan korisnik je pokusao da se vec postojecim mobilnim telefonom..");
            throw new UserAlreadyExistsException("User with that telephone number already exists!");
        }
        if(!registrationDTO.getPassword().equals(registrationDTO.getRepeatPassword())){
            logger.error("Neregistrovan korisnik je pokusao da se registruje sa razlicitim lozinkama.");

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

    @Override
    public void sendPasswordResetCode(String email, VerifyType verifyType) throws MessagingException, IOException {

        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isEmpty())
            throw new NonExistingUserException("User with that email does not exist!");

        Random random = new Random();
        String code = String.format("%05d", random.nextInt(100000));
        user.get().setPasswordResetCode(new ResetCode(code, LocalDateTime.now().plusMinutes(15)));
        if (verifyType.equals(VerifyType.EMAIL)){
            sendPasswordResetEmail(user.get());
        }
        else if(verifyType.equals(VerifyType.SMS)){
            sendPasswordResetSms(user.get());
        }

        this.userRepository.save(user.get());
    }

    @Override
    public void resetPassword(String email, PasswordResetDTO passwordResetDTO) {

        Optional<User> user = this.userRepository.findByEmail(email);

        if (user.isEmpty()) {

            throw new NonExistingUserException("User with that email does not exist!");
        }
        if (!user.get().getPasswordResetCode().getCode().equals(passwordResetDTO.getCode()) || user.get().getPasswordResetCode().getExpirationDate().isBefore(LocalDateTime.now())) {
            logger.warn("Kod je nevalidan ili istekao.");

            throw new InvalidResetCodeException("Code is invalid or it expired!");

        }

        if(!passwordResetDTO.getPassword().equals(passwordResetDTO.getRepeatPassword())) {
            logger.warn("Lozinka i ponovljena lozinka se ne podudaraju");

            throw new InvalidRepeatPasswordException("Password and repeat password must be same!");
        }

        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        String encodedPw = passwordEncoder.encode(passwordResetDTO.getPassword());
        System.out.println("PW FIRST: " + passwordEncoder.encode(passwordResetDTO.getPassword()));
        System.out.println("PW FIRST: " + user.get().getPassword());

        if(passwordEncoder.matches(passwordResetDTO.getPassword(),user.get().getPassword())){
            throw new InvalidNewPasswordException("Please enter a different password.");
        }
        List<PastPasswordsDTO> passwords = this.pastPasswordRepository.findPastPasswordsByUserId(user.get().getId().longValue());
        if(passwords.size() <= 3){
            for(PastPasswordsDTO pp: passwords){
                if(passwordEncoder.matches(passwordResetDTO.getPassword(), pp.getPassword()))throw new InvalidNewPasswordException("Please enter a different password.");
            }
        }else{
            List<PastPasswordsDTO> lastPasswords = passwords.subList(passwords.size()-3, passwords.size());
            for(PastPasswordsDTO pp: lastPasswords){
                if(passwordEncoder.matches(passwordResetDTO.getPassword(), pp.getPassword()))throw new InvalidNewPasswordException("Please enter a different password.");
            }
        }

        this.pastPasswordRepository.save(
                new PastPasswords(user.get(), user.get().getPassword(), LocalDateTime.now()));

        user.get().setPassword(encodedPw);
        user.get().setLastTimePasswordChanged(LocalDateTime.now());
        this.userRepository.save(user.get());

    }

    @Override
    public void sendLoginVerification(String email, VerifyType verifyType) throws MessagingException, IOException {

        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isEmpty())
            throw new NonExistingUserException("User with that email does not exist!");
        Random random = new Random();
        String code = String.format("%05d", random.nextInt(100000));
        user.get().setLoginVerification(new Verification(code, LocalDateTime.now().plusMinutes(15)));
        if (verifyType.equals(VerifyType.EMAIL)){
            sendLoginVerifyEmail(user.get());
        }
        else if(verifyType.equals(VerifyType.SMS)){
            sendLoginVerifySms(user.get());
        }

        this.userRepository.save(user.get());
    }

    @Override
    public void loginVerify(String email, LoginVerifyCodeDTO code) {
        Optional<User> user = this.userRepository.findByEmail(email);
        if (user.isEmpty()) throw new NonExistingUserException("User with that email does not exist!");
        if (!user.get().getLoginVerification().getVerificationCode().equals(code.getCode()) || user.get().getLoginVerification().getExpirationDate().isBefore(LocalDateTime.now())) {
            logger.warn("Kod za logovanje koji je uneo korisnik je ili nevalidan ili istekao");

            throw new InvalidResetCodeException("Code is invalid or it expired!");
        }

    }

    @Override
    public boolean verifyRecaptcha(String recaptcha) {
        String url = "https://www.google.com/recaptcha/api/siteverify";
        Dotenv dotenv = Dotenv.load();

        String params = "?secret="+dotenv.get("RECAPTCHA_KEY")+"&response="+recaptcha;
        RecaptchaResponse recaptchaResponse = restTempate.exchange(url+params, HttpMethod.POST, null, RecaptchaResponse.class).getBody();
        if(recaptchaResponse == null) {
            logger.warn("Korisnik nije ispravno uneo recaptchu.");

            throw new InvalidRecaptchaException("Something went wrong with the recaptcha. Please try again.");
        }
        return recaptchaResponse.isSuccess();
    }

    @Override
    public boolean isPasswordDurationValid(String email) {
        Optional<LocalDateTime> time = this.userRepository.findLastTimePasswordChanged(email);
        if(time.isEmpty()) throw new NonExistingUserException("The user with the given id does not exist.");
        // optimal change is roughtly 3 months
        return LocalDateTime.now().minusMonths(3).isBefore(time.get());
    }

    @Override
    public Boolean oauthDoesMailExists(OauthUserDTO oauthUser) {
        return this.emailExists(oauthUser.getEmail());
    }

    @Override
    public RegisteredUserDTO regsterOauth(OauthUserDTO userDTO) {
        User user = getUserFromOauthUserDTO(userDTO);

        logger.info("Neulogovan korisnik se uspesno registrovao preko Oauth protokola");
        return new RegisteredUserDTO(user);
    }


    private void sendPasswordResetSms(User user) {
        try {
            Message.creator(
                            new PhoneNumber(user.getTelephoneNumber()),
                            new PhoneNumber(twilioConfiguration.getPhoneNumber()),
                            "Your password reset code is: " + user.getPasswordResetCode().getCode())
                    .create();
        }
        catch (com.twilio.exception.ApiException ex)
        {
            throw new InvalidPhoneException("Can't send message if phone is not verified and valid!");
        }
    }


    private void sendLoginVerifySms(User user) {
        try {
            Message.creator(
                            new PhoneNumber(user.getTelephoneNumber()),
                            new PhoneNumber(twilioConfiguration.getPhoneNumber()),
                            "Your login verification code is: " + user.getLoginVerification().getVerificationCode())
                    .create();
        }
        catch (com.twilio.exception.ApiException ex)
        {
            throw new InvalidPhoneException("Can't send message if phone is not verified and valid!");
        }
    }

    

  private void sendPasswordResetEmail(User user) throws MessagingException, IOException {

        Email from = new Email("tim9certificates@gmail.com");
        String subject = "Reset code for certificate app";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/plain", "Dear " + user.getName() + ","
                + "Below you can find your code for changing your password: \n"
                 + user.getPasswordResetCode().getCode() + "\n"
                + "Have a nice day!,\n"
                + "Certificate app.");
        Mail mail = new Mail(from, subject, to, content);
        Dotenv dotenv = Dotenv.load();
        SendGrid sg = new SendGrid(dotenv.get("SENDGRID_API_KEY"));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }
    private void sendLoginVerifyEmail(User user) throws MessagingException, IOException {
        Email from = new Email("tim9certificates@gmail.com");
        String subject = "Login verify code for certificate app";
        Email to = new Email(user.getEmail());
        Content content = new Content("text/plain", "Dear " + user.getName() + ","
                + "Below you can find your code for login verify: \n"
                + user.getLoginVerification().getVerificationCode() + "\n"
                + "Have a nice day!,\n"
                + "Certificate app.");
        Mail mail = new Mail(from, subject, to, content);
        Dotenv dotenv = Dotenv.load();
        SendGrid sg = new SendGrid(dotenv.get("SENDGRID_API_KEY"));

        Request request = new Request();
        try {
            request.setMethod(Method.POST);
            request.setEndpoint("mail/send");
            request.setBody(mail.build());
            Response response = sg.api(request);
        } catch (IOException ex) {
            throw ex;
        }
    }


    private User getUserFromRegistrationDTO(UserDTO registrationDTO) throws MessagingException, IOException {
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
        String code = String.format("%05d", random.nextInt(100000));
        user.setVerification(new Verification(code, LocalDateTime.now().plusDays(3)));
        user.setEmailConfirmed(false);
        if (registrationDTO.getVerifyType().equals(VerifyType.EMAIL)){
            sendVerificationEmail(user);
        }
        else if(registrationDTO.getVerifyType().equals(VerifyType.SMS)){
            sendSmsVerification(user);
        }
        user = this.userRepository.save(user);

        return user;
    }
    private User getUserFromOauthUserDTO(OauthUserDTO oauthUserDTO){
        User user = new User();
        user.setEmail(oauthUserDTO.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

        user.setPassword(passwordEncoder.encode("SifraSifraSifra123!"));
        user.setRole(UserRole.BASIC_USER);
        user.setTelephoneNumber("+381659715120");
        user.setSurname(oauthUserDTO.getSurname());
        user.setName(oauthUserDTO.getName());
        user.setLastTimePasswordChanged(LocalDateTime.now());
        user.setEmailConfirmed(true);
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
