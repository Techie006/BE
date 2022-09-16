package com.sparta.cookbank.domain.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class IngredientsByCategoryResponseDto {
    private List<Integer> count;
}
