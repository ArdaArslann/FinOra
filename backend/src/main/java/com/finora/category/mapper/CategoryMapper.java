package com.finora.category.mapper;


import com.finora.category.dto.CategoryResponse;
import com.finora.category.dto.CreateCategoryRequest;
import com.finora.category.entity.CategoryEntity;
import com.finora.user.entity.UserEntity;
import org.springframework.stereotype.Component;

@Component
public class CategoryMapper {
    public CategoryResponse toResponse(CategoryEntity categoryEntity) {
        return new CategoryResponse(
                categoryEntity.getId(),
                categoryEntity.getName(),
                categoryEntity.getIcon(),
                categoryEntity.getColor(),
                categoryEntity.isDefaultCategory()
        );
    }

    public CategoryEntity toEntity(

            CreateCategoryRequest request,
            UserEntity user

    ) {

        return CategoryEntity.create(

                request.name(),

                request.icon(),

                request.color(),

                false,

                user

        );

    }

}
