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
public class RecipeAllBookmarkResponseDto {
    private int current_page_num;
    private int total_page_num;
    private String user_name;
    private List<RecipeBookmarkResponseDto> recipes;
}
