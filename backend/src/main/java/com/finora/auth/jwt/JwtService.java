package com.finora.auth.jwt;

import com.finora.user.entity.UserEntity;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Date;

public interface JwtService {
    String generateToken(UserEntity user);

    String extractEmail(String token);

    Date extractExpiration(String token);

    boolean isTokenValid(String token, UserDetails user);
}
