package com.finora.auth.jwt;

import com.finora.user.entity.UserEntity;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import java.util.Date;

@Service
public class JwtServiceImpl implements  JwtService{


   @Value("${jwt.secret}")
   private String secret;

   @Value("${jwt.expiration}")
   private Long expiration;

    @Override
    public String generateToken(UserEntity user) {
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.builder().subject(user.getEmail())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis()+expiration))
                .signWith(secretKey).compact();
    }

    private Claims extractAllClaims(String token){
        SecretKey secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

        return Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    @Override
    public String extractEmail(String token){
        return extractAllClaims(token).getSubject();
    }

    @Override
    public Date extractExpiration(String token){
        return extractAllClaims(token).getExpiration();
    }

    @Override
    public boolean isTokenValid(String token, UserDetails user){
        return !isTokenExpired(token)
                && extractEmail(token).equals(user.getUsername());

    }

    boolean isTokenExpired(String token){
        return extractExpiration(token).before(new Date());
    }
}
