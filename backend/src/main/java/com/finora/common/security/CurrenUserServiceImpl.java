package com.finora.common.security;

import com.finora.common.exception.BusinessException;
import com.finora.user.entity.UserEntity;
import com.finora.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CurrenUserServiceImpl implements CurrentUserService {

    private final UserRepository userRepository;

    @Override
    public UserEntity getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()
                || authentication instanceof AnonymousAuthenticationToken) {
            throw new BusinessException("UNAUTHORIZED_ACCESS", "Unauthorized access");
        }

        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        return userRepository.findByEmail(userDetails.getUsername())
                .orElseThrow(() -> new BusinessException("USER_NOT_FOUND", "User not found"));
    }
}
