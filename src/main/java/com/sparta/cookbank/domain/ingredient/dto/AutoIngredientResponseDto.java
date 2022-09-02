package com.sparta.cookbank.domain.ingredient.dto;

import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoIngredientResponseDto {
    private List<IngredientResponseDto> auto_complete;
}
