package com.finora.user.entity;

import com.finora.common.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class UserEntity extends BaseEntity {

    @Column(nullable = false, length = 100)
    private String firstName;

    @Column(nullable = false, length = 100)
    private String lastName;

    @Column(nullable = false, unique = true, length = 255)
    private String email;

    @Column(nullable = false, length = 255)
    private String password;

    public static UserEntity create(String firstName, String lastName, String email, String password) {
        UserEntity userEntity = new UserEntity();
        userEntity.email = email;
        userEntity.firstName = firstName;
        userEntity.lastName = lastName;
        userEntity.password = password;

        return userEntity;
    }
}
