package com.finora.category.entity;

import com.finora.common.entity.BaseEntity;
import com.finora.user.entity.UserEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "categories")
public class CategoryEntity extends BaseEntity {

    @Column(nullable = false, length = 50)
    private String name;

    @Column(nullable = false, length = 20)
    private String color;

    @Column(nullable = false, length = 50)
    private String icon;

    @Column(nullable = false)
    private boolean defaultCategory;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private UserEntity user;

    public static CategoryEntity create(
            String name,
            String icon,
            String color,
            boolean defaultCategory,
            UserEntity user
    ){
        CategoryEntity categoryEntity = new CategoryEntity();

        categoryEntity.name = name;
        categoryEntity.color = color;
        categoryEntity.icon = icon;
        categoryEntity.defaultCategory = defaultCategory;
        categoryEntity.user = user;

        return categoryEntity;
    }

    public void update(
            String name,
            String icon,
            String color
    ) {
        this.name = name;
        this.icon = icon;
        this.color = color;
    }


}
