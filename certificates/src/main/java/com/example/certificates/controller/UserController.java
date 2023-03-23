package com.example.certificates.controller;

import com.example.certificates.dto.LoginDTO;
import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.TokenDTO;
import com.example.certificates.dto.UserDTO;
import com.example.certificates.model.ErrorResponseMessage;
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

import javax.validation.Valid;
import java.util.Locale;

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

    @Autowired
    public UserController(IUserService userService, MessageSource messageSource){

        this.userService = userService;
        this.messageSource = messageSource;
    }

    @PostMapping("/register")
    public ResponseEntity<RegisteredUserDTO> register(@Valid @RequestBody UserDTO userDTO){


        RegisteredUserDTO newUser = this.userService.register(userDTO);

        return new ResponseEntity<>(newUser, HttpStatus.OK);

    }

    @PostMapping("/login")
    public ResponseEntity<TokenDTO> logIn(@Valid @RequestBody LoginDTO login) {
        try {

            TokenDTO token = new TokenDTO();
            SecurityUser userDetails = (SecurityUser) this.userService.findByUsername(login.getEmail());


            boolean isEmailConfirmed = this.userService.getIsEmailConfirmed(login.getEmail());
            if(!isEmailConfirmed){
                return new ResponseEntity(new ErrorResponseMessage(
                        this.messageSource.getMessage("user.emailNotConfirmed", null, Locale.getDefault())
                ), HttpStatus.BAD_REQUEST);
            }

            String tokenValue = this.jwtTokenUtil.generateToken(userDetails);
            token.setToken(tokenValue);
            Authentication authentication =
                    this.authenticationManager.authenticate(
                            new UsernamePasswordAuthenticationToken(login.getEmail(),
                                    login.getPassword()));

            SecurityContextHolder.getContext().setAuthentication(authentication);

            return new ResponseEntity<>(token, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity(new ErrorResponseMessage(
                    this.messageSource.getMessage("user.badCredentials", null, Locale.getDefault())
            ), HttpStatus.BAD_REQUEST);
        }

    }
}
