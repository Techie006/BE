package com.sparta.cookbank.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RecipeRecommendResponseDto {
    private Long id;
    private String recipe_name;
    private String ingredients;
    private String method;
    private String category;
    private Long calorie;
}
