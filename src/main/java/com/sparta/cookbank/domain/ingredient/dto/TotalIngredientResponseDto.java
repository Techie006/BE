package com.sparta.cookbank.domain.ingredient.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TotalIngredientResponseDto {
    private int total_count;
    private List<IngredientResponseDto> search_list;
}
