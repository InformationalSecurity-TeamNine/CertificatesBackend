package com.example.certificates.service;

import com.example.certificates.dto.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import com.example.certificates.enums.UserRole;
import com.example.certificates.exceptions.InvalidRepeatPasswordException;
import com.example.certificates.exceptions.UserAlreadyExistsException;
import com.example.certificates.model.User;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Override
    public RegisteredUserDTO register(UserDTO registrationDTO) {

        checkValidUserInformation(registrationDTO);
        User user = getUserFromRegistrationDTO(registrationDTO);
        return new RegisteredUserDTO(user);
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

    private User getUserFromRegistrationDTO(UserDTO registrationDTO) {
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();
        user.setPassword(passwordEncoder.encode(registrationDTO.getPassword()));
        user.setRole(UserRole.BASIC_USER);
        user.setTelephoneNumber(registrationDTO.getTelephoneNumber());
        user.setSurname(registrationDTO.getSurname());
        user.setName(registrationDTO.getName());
        user.setEmailConfirmed(false);
        user.setLastTimePasswordChanged(LocalDateTime.now());
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
