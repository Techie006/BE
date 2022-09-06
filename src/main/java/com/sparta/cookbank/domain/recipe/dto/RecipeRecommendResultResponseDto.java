package com.sparta.cookbank.domain.recipe.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RecipeRecommendResultResponseDto {
    private List<RecipeRecommendResponseDto> recipes;
}
