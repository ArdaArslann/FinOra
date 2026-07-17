package com.finora.auth.service;

import com.finora.auth.dto.LoginRequest;
import com.finora.auth.dto.LoginResponse;
import com.finora.auth.dto.RegisterRequest;
import com.finora.auth.dto.RegisterResponse;
import com.finora.auth.jwt.JwtService;
import com.finora.user.entity.UserEntity;
import com.finora.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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

    @Override
    public LoginResponse login(LoginRequest loginRequest) {
        UserEntity user = userRepository.findByEmail(loginRequest.email())
                .orElseThrow(()->
                     new RuntimeException("Invalid email or password")
                );

       boolean isPasswordCorrect =  passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

       if(isPasswordCorrect){
           return new LoginResponse(jwtService.generateToken(user));
       }
       else{
            throw new RuntimeException("Invalid email or password.");
       }
    }


}
