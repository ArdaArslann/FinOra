package com.finora.common.security;

import com.finora.user.entity.UserEntity;

public interface CurrentUserService {
    UserEntity getCurrentUser();
}
