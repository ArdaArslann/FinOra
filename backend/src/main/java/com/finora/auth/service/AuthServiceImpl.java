package com.finora.auth.service;

import com.finora.auth.dto.*;
import com.finora.auth.entity.RefreshTokenEntity;
import com.finora.auth.jwt.JwtService;
import com.finora.common.exception.BusinessException;
import com.finora.user.entity.UserEntity;
import com.finora.user.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;


@Service
public class AuthServiceImpl implements AuthService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;

    public AuthServiceImpl(UserRepository userRepository, PasswordEncoder passwordEncoder, JwtService jwtService, RefreshTokenService refreshTokenService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.refreshTokenService = refreshTokenService;
    }

    @Override
    public RegisterResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByEmail(registerRequest.email())) {
            throw new BusinessException("E-mail exists");
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
                        new BusinessException("Invalid email or password"));

       boolean isPasswordCorrect =  passwordEncoder.matches(
                loginRequest.password(),
                user.getPassword()
        );

        if(isPasswordCorrect){

            String accessToken = jwtService.generateToken(user);

            RefreshTokenEntity refreshToken =
                    refreshTokenService.createRefreshToken(user);


            return new LoginResponse(
                    accessToken,
                    refreshToken.getToken()
            );
        }
       else{
           throw new BusinessException("Invalid email or password");
       }
    }

    @Override
    public RefreshTokenResponse refresh(RefreshTokenRequest request) {

        RefreshTokenEntity refreshToken =
                refreshTokenService.verifyRefreshToken(
                        request.refreshToken()
                );


        UserEntity user = refreshToken.getUser();


        String accessToken = jwtService.generateToken(user);


        return new RefreshTokenResponse(accessToken);
    }

    @Override
    public void logout(String refreshToken) {

        refreshTokenService.deleteByToken(refreshToken);

    }


}
