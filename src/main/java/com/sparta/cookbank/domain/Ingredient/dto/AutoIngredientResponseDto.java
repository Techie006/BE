package com.sparta.cookbank.domain.Ingredient.dto;

import com.sparta.cookbank.domain.myingredients.dto.MyIngredientResponseDto;
import lombok.*;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoIngredientResponseDto {
    private List<IngredientResponseDto> auto_complete;
}
