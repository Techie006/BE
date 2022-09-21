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
    private int current_page_num;
    private int total_page_num;
    private List<IngredientResponseDto> search_list;
}
