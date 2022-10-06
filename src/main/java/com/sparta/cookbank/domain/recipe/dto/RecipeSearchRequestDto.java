package com.sparta.cookbank.domain.recipe.dto;

import lombok.*;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
@EqualsAndHashCode
public class RecipeSearchRequestDto {
    private String recipe_name;
}
