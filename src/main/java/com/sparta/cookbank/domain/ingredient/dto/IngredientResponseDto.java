package com.sparta.cookbank.domain.ingredient.dto;

import com.sparta.cookbank.domain.ingredient.enums.FoodCategory;
import lombok.*;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class IngredientResponseDto {

    private Long id;
    private String food_name;
    private FoodCategory group_name;
}
