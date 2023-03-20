package com.example.certificates.service;

import com.example.certificates.dto.UserDTO;
import com.example.certificates.model.User;
import com.example.certificates.repository.UserRepository;
import com.example.certificates.service.interfaces.IUserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService implements IUserService {
    private final UserRepository userRepository;

    @Autowired
    public UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }
    @Override
    public UserDTO register(UserDTO registrationDTO) {
        // check if email exists, fields are validated in controller
        // check if phone number exists
        User user = new User();
        this.userRepository.save(user);
        return null;
    }
}
