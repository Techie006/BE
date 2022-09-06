package com.sparta.cookbank.domain.doneRecipe.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DoneRecipeRequestDto {
    List<Long> ingredients_id;
}
