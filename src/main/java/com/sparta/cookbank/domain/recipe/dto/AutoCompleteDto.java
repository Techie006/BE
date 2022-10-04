package com.sparta.cookbank.domain.recipe.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AutoCompleteDto {
    private Long id;
    private String recipe_name;
}
