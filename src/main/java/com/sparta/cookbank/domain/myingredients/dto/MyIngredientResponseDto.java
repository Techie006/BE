package com.sparta.cookbank.domain.myingredients.dto;

import com.sparta.cookbank.domain.ingredient.enums.FoodCategory;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MyIngredientResponseDto {
    private Long id;
    private String icon_image;
    private String mark_name;
    private String food_name;
    private FoodCategory group_name;
    private String in_date;
    private String d_date;
}
