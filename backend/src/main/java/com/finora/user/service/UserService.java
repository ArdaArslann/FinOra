package com.finora.user.service;

import com.finora.user.dto.UserResponse;
import com.finora.user.entity.UserEntity;

public interface UserService {
    UserResponse getCurrentUser();

}
