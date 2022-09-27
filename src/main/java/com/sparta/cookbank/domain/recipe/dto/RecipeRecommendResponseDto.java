package com.sparta.cookbank.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Builder
public class RecipeRecommendResponseDto {
    private Long id;
    private String recipe_name;
    private String recipe_image;
    private List<String> common_ingredients;
    private List<String> ingredients;
    private String method;
    private String category;
    private Long calorie;
    private boolean liked;
}
