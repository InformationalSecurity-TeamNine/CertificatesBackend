package com.example.certificates.controller;

import com.example.certificates.dto.*;
import com.example.certificates.enums.VerifyType;
import com.example.certificates.model.ErrorResponseMessage;
import com.example.certificates.model.User;
import com.example.certificates.model.Verification;
import com.example.certificates.security.ErrorResponse;
import com.example.certificates.security.JwtTokenUtil;
import com.example.certificates.security.SecurityUser;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.MessageSource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;


import javax.mail.MessagingException;
import javax.persistence.criteria.Predicate;
import javax.validation.Valid;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Map;
import java.util.Random;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
@CrossOrigin
@RestController
@RequestMapping("api/user")
public class UserController {

    private final IUserService userService;
    private final MessageSource messageSource;

    @Autowired
    private JwtTokenUtil jwtTokenUtil;
    @Autowired
    private AuthenticationManager authenticationManager;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(IUserService userService, MessageSource messageSource){

        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserDTO> register(@Valid @RequestBody UserDTO userDTO) throws IOException, MessagingException {
        logger.info("Neregistrovan korisnik je pokrenuo funkcionalnost registracije uspesno");

        RegisteredUserDTO newUser = this.userService.register(userDTO);
        logger.info("Korisnik se uspesno registrovao");

        return new ResponseEntity<>(newUser, HttpStatus.OK);
    }

    @PostMapping("/oauth")
    public ResponseEntity<Object> oauthSignIn(@Valid @RequestBody OauthUserDTO userDTO){
        logger.info("Neulogovan korisnik je pokrenuo funkcionalnost logovanja pomocu Oauth protokola");

        Boolean isRegistered = this.userService.oauthDoesMailExists(userDTO);
        if (isRegistered){

            TokenDTO token = new TokenDTO();
            SecurityUser userDetails = (SecurityUser) this.userService.findByUsername(userDTO.getEmail());
            String tokenValue = this.jwtTokenUtil.generateToken(userDetails);
            token.setToken(tokenValue);

            logger.info("Neulogovan korisnik se uspesno ulogovao preko oauth protokola");
            return new ResponseEntity<>(token, HttpStatus.OK);
        }else{
            RegisteredUserDTO newUser = this.userService.regsterOauth(userDTO);

            logger.info("Neulogovan korisnik se uspesno ulogovao preko oauth protokola");
            return new ResponseEntity<>(newUser, HttpStatus.OK);
        }

    }

    @PostMapping(value = "/{email}/resetPassword")
    public ResponseEntity<String> sendPasswordResetEmail(@PathVariable("email") String email, @RequestBody VerifyTypeResetDTO verifyType) throws Exception {

        userService.sendPasswordResetCode(email, verifyType.getVerifyType());
        logger.info("Neulogovanom korisniku je poslat kod za reset lozinke");

        return new ResponseEntity<>("Email with reset code has been sent!",HttpStatus.NO_CONTENT);
    }

    @PutMapping(value = "/{email}/resetPassword")
    public ResponseEntity<String> resetPassword(@PathVariable("email") String email, @Valid @RequestBody PasswordResetDTO passwordResetDTO) throws Exception {
        logger.info("Neulogovan korisnik je pokrenuo funkcionalnost resetovanja lozinke");

        userService.resetPassword(email, passwordResetDTO);
        logger.info("Neulogovanom korisniku je uspesno resetovana lozinka.");

        return new ResponseEntity<>("Password successfully changed!",HttpStatus.OK);
    }

    @GetMapping(value = "/activate/{idActivation}")
    public ResponseEntity<String> activateUserEmail(@PathVariable("idActivation") String verificationCode) {
        logger.info("Korisnik je pokrenuo akciju za verifikaciju naloga preko maila");

        userService.verifyUser(verificationCode);
        return new ResponseEntity<>(("Account activated!"),HttpStatus.OK);
    }
    @PutMapping(value = "/activate/{idActivation}")
    public ResponseEntity<String> activateUser(@PathVariable("idActivation") String verificationCode) {
        logger.info("Korisnik je pokrenuo akciju za verifikaciju naloga preko telefona");

        userService.verifyUser(verificationCode);
        return new ResponseEntity<>(("Account activated!"),HttpStatus.OK);
    }


    @PostMapping("/login")
    public ResponseEntity<String> logIn(@Valid @RequestBody LoginDTO login) throws MessagingException, IOException {
        logger.info("Korisnik je pokrenuo akciju za logovanje.");

        try {

            Authentication authentication =
                    this.authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(login.getEmail(),
                                    login.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        } catch (Exception e) {

            return new ResponseEntity(new ErrorResponseMessage(
                    this.messageSource.getMessage("user.badCredentials", null, Locale.getDefault())
            ), HttpStatus.BAD_REQUEST);
        }
            boolean isEmailConfirmed = this.userService.getIsEmailConfirmed(login.getEmail());
            if(!isEmailConfirmed){
                return new ResponseEntity(new ErrorResponseMessage(
                        this.messageSource.getMessage("user.emailNotConfirmed", null, Locale.getDefault())
                ), HttpStatus.BAD_REQUEST);
            }
            userService.sendLoginVerification(login.getEmail(), login.getType());
        logger.info("Korisnik se uspesno ulogovao.");

        return new ResponseEntity<>(("Succesfully sent login validation code!"), HttpStatus.OK);


    }

    @PostMapping("/login/{email}/verify")
    public ResponseEntity<TokenDTO> loginVerification(@PathVariable("email") String email, @RequestBody LoginVerifyCodeDTO code){
        logger.info("Korisnik je pokrenuo akciju za verifikaciju naloga preko emaila");

        userService.loginVerify(email, code);
        TokenDTO token = new TokenDTO();
        SecurityUser userDetails = (SecurityUser) this.userService.findByUsername(email);
        String tokenValue = this.jwtTokenUtil.generateToken(userDetails);
        token.setToken(tokenValue);

        return new ResponseEntity<>(token, HttpStatus.OK);

    }

    @PostMapping(value = "/recaptcha")
    public ResponseEntity<Boolean> validateRecaptcha(@RequestParam("g-recaptcha-response")String recaptcha){
        logger.info("Korisnik je pokrenuo akciju za verifikaciju recaptche");

        return new ResponseEntity<>(this.userService.verifyRecaptcha(recaptcha), HttpStatus.OK);
    }


    @GetMapping(value = "/password-expired/{email}")
    public ResponseEntity<Boolean> isPasswordDurationValid(@PathVariable("email") String email){
        logger.info("Pokrenuta je akcija za proveru da li je lozinka idalje validna");

        return new ResponseEntity<>(this.userService.isPasswordDurationValid(email), HttpStatus.OK);
    }
}
