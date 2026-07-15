package com.finora.auth.service;

import com.finora.auth.dto.RegisterRequest;
import com.finora.auth.dto.RegisterResponse;
import com.finora.user.entity.UserEntity;
import com.finora.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;

    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new RuntimeException("E-mail exists");
        }

        String encodedPassword = passwordEncoder.encode(registerRequest.password());

        UserEntity user = UserEntity.create(
                registerRequest.firstName(),
                registerRequest.lastName(),
                registerRequest.email(),
                encodedPassword
        );

        userRepository.save(user);
        return new RegisterResponse("Registration successful.");
    }


}
