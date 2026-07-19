package com.finora.user.service;

import com.finora.common.exception.ResourceNotFoundException;
import com.finora.user.dto.UserResponse;
import com.finora.user.entity.UserEntity;
import com.finora.user.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    public UserServiceImpl(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public UserResponse getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String email = authentication.getName();
        UserEntity user = userRepository.findByEmail(email).orElseThrow(() ->
        new ResourceNotFoundException("REFRESH_TOKEN_NOT_FOUND","User not found"));

        return new UserResponse(user.getFirstName(),user.getLastName(),user.getEmail(),user.getId());
    }
}
