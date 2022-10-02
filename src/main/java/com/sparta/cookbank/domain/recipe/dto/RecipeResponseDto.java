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
public class RecipeResponseDto {
    private int current_page_num;
    private int total_page_num;
    private List<RecipeAllResponseDto> recipes;
    private String search_name;
}
