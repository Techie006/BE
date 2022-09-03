package com.sparta.cookbank.domain.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class IngredientsByCategoryResponseDto {
    private int starch_num;
    private int nut_num;
    private int cereal_num;
    private int fruit_num;
    private int etc_num;
    private int nan_num;
    private int sugar_num;
    private int pulses_num;
    private int mushroom_num;
    private int fish_num;
    private int milkProducts_num;
    private int fatAndOils_num;
    private int meat_num;
    private int drink_num;
    private int processedFood_num;
    private int seasoning_num;
    private int alcohol_num;
    private int tea_num;
    private int vegetable_num;
    private int seaweed_num;
}
