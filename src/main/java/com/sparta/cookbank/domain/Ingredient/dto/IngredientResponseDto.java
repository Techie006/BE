package com.sparta.cookbank.domain.Ingredient.dto;

import com.sparta.cookbank.domain.Ingredient.enums.FoodCategory;
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
