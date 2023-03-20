package com.example.certificates.service;

import com.example.certificates.controller.RegisteredUserDTO;
import com.example.certificates.dto.UserDTO;
import com.example.certificates.enums.UserRole;
import com.example.certificates.model.User;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

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

        if(this.emailExists(registrationDTO.getEmail()))
        {
            // do logic throw exception
            return null;
        }
        if(this.telephoneNumberExists(registrationDTO.getTelephoneNumber())){
            // do logic
            return null;
        }
        if(!registrationDTO.getPassword().equals(registrationDTO.getRepeatPassword())){
            // do logic
            return null;
        }
        User user = getUserFromRegistrationDTO(registrationDTO);

        return new RegisteredUserDTO(user);
    }

    @Override
    public UserDetails findByUsername(String username) {
        Optional<User> user = this.userRepository.findByEmail(username);
        if(user.isEmpty()) return null;
        return UserFactory.create(user.get());
    }

    @Override
    public boolean getIsEmailConfirmed(String email) {
        return this.userRepository.findByEmail(email).isPresent();
    }

    private User getUserFromRegistrationDTO(UserDTO registrationDTO) {
        User user = new User();
        user.setEmail(registrationDTO.getEmail());
        user.setPassword(registrationDTO.getPassword());
        user.setRole(UserRole.REGISTERED);
        user.setTelephoneNumber(registrationDTO.getTelephoneNumber());
        user.setSurname(registrationDTO.getSurname());
        user.setName(registrationDTO.getName());
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
