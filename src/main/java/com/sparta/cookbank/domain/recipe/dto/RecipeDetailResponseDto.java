package com.sparta.cookbank.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class RecipeDetailResponseDto {
    private Long id;
    private String recipe_name;
    private List<String> ingredients;
    private String method;
    private String category;
    private Long calorie;
    private Long calbohydrates;
    private Long proteins;
    private Long fats;
    private Long sodium;
    private String final_img;
    private List<String> manual_desc;
    private List<String> manual_imgs;
}
