package com.finora.auth.jwt;

import com.finora.auth.security.CustomUserDetailsService;
import java.io.IOException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            CustomUserDetailsService userDetailsService) {

        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain)
            throws ServletException, IOException {

            String authHeader = request.getHeader("Authorization");
            System.out.println("REQUEST: "

                + request.getMethod()

                + " "

                + request.getRequestURI());

            System.out.println("AUTH HEADER: " + authHeader);

            boolean isAuthInvalid = authHeader == null || !authHeader.startsWith("Bearer ");

            if(isAuthInvalid) {
                filterChain.doFilter(request,response);
                return;
            }

                String jwt =  authHeader.substring(7);
                String email = jwtService.extractEmail(jwt);
                UserDetails userDetails = userDetailsService.loadUserByUsername(email);
                if(jwtService.isTokenValid(jwt, userDetails)) {
                    UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,null, userDetails.getAuthorities());
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
                filterChain.doFilter(request,response);


    }
}